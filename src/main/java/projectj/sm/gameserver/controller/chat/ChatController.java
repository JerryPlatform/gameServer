package projectj.sm.gameserver.controller.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.RedisUtil;
import projectj.sm.gameserver.domain.Room;
import projectj.sm.gameserver.dto.ChatRoomDto;
import projectj.sm.gameserver.dto.ChattingMessageDto;
import projectj.sm.gameserver.dto.SecretChatRoomVerificationDto;
import projectj.sm.gameserver.service.ChatRoomService;
import projectj.sm.gameserver.vo.UserChatSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
@RestController
@EnableScheduling
@RequiredArgsConstructor
public class ChatController {

    private final RedisUtil redisUtil;

    private final SimpMessagingTemplate template;

    private final ChatRoomService chatRoomService;

    public static List<UserChatSession> UserChatSessions = new ArrayList<>();

    @PostMapping("/secret/chatroom/verification")
    public Boolean secretChatRoomVerification(@RequestBody SecretChatRoomVerificationDto dto) {
        return chatRoomService.SecretChatRoomVerification(dto);
    }

    @PutMapping("/chatroom")
    public void createChatRoom(@RequestBody ChatRoomDto dto) throws JsonProcessingException {
        chatRoomService.createChatRoom(dto);
        updateChatRoomList(dto.getType());
    }

    @DeleteMapping("/{id}/chatroom")
    public void removeChatRoom(@PathVariable Long id) throws JsonProcessingException {
        Room.Type type = chatRoomService.findByChatRoom(id).getType();
        chatRoomService.removeChatRoom(id);
        updateChatRoomList(type);
    }

    @MessageMapping("/message/chatroom")
    public void sendChattingMessage(ChattingMessageDto dto, @Header("simpSessionId") String simpSessionId) throws JsonProcessingException {
        Room room = chatRoomService.findByChatRoom(dto.getChatRoomId());
        UserChatSession userInfo = getSessionUser(simpSessionId);

        Map<String, Object> messageMapping = new HashMap<>();
        messageMapping.put("userName", userInfo.getUserName());
        messageMapping.put("messageContent", dto.getMessageContent());
        messageMapping.put("date", CommonUtil.getLocalTime());
        messageMapping.put("authority", "user");

        String message = CommonUtil.objectToJsonString(messageMapping);
        template.convertAndSend("/sub/chatting/chatroom/" + room.getId(), message);
    }

    public void updateChatRoomList(Room.Type type) throws JsonProcessingException {
        List<Room> roomList = chatRoomService.getChatRoomListByType(type);
        List<Map<String, Object>> chatRoomInfos = new ArrayList<>();

        for (Room room : roomList) {
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("chatRoomId", room.getId());
            roomInfo.put("chatRoomName", room.getRoomName());
            roomInfo.put("userCount", UserChatSessions.stream().filter(userChatSession -> userChatSession.getChatRoomId().equals(room.getId())).count());
            roomInfo.put("private", room.getPassword() != null ? true : false);
            chatRoomInfos.add(roomInfo);
        }

        String resultValue = CommonUtil.objectToJsonString(chatRoomInfos);
        template.convertAndSend("/sub/chatroom/list/" + type, resultValue);
    }

    public void updateChatRoomListAll() throws JsonProcessingException {
        for (Room.Type type : Room.Type.values()) {
            updateChatRoomList(type);
        }
    }

    @EventListener
    public void sessionSubscribeEvent(SessionSubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String subscribeAddress = CommonUtil.extractDataFromEventMessages(event, "destination");

        if (subscribeAddress.contains("/sub/chatroom/list/")) {
            updateChatRoomListAll();
        }

        if (subscribeAddress.contains("/sub/chatting/chatroom/")) {
            Long chatRoomId = Long.parseLong(subscribeAddress.split("/sub/chatting/chatroom/")[1]);
            createUserChatSession(chatRoomId, simpSessionId);
            userChatRoomSessionSynchroization(chatRoomId);
            UserChatSession userInfo = getSessionUser(simpSessionId);

            String message = notificationMessageMapping(userInfo.getUserName() + "님이 채팅방에 입장하였습니다.");
            template.convertAndSend("/sub/chatting/chatroom/" + chatRoomId, message);
        }
    }

    @EventListener
    public void SessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String subscribeAddress = CommonUtil.extractDataFromEventMessages(event, "destination");

        if (subscribeAddress.contains("/sub/chatting/chatroom/")) {
            UserChatSession userInfo = getSessionUser(simpSessionId);
            Long chatRoomId = Long.parseLong(subscribeAddress.split("/sub/chatting/chatroom/")[1]);
            removeUserChatSession(simpSessionId);
            userChatRoomSessionSynchroization(chatRoomId);

            String message = notificationMessageMapping(userInfo.getUserName() + "님이 채팅방에 퇴장하였습니다.");
            template.convertAndSend("/sub/chatting/chatroom/" + chatRoomId, message);
        }
    }

    public void createUserChatSession(Long roomId, String simpSessionId) throws JsonProcessingException {
        Map<String, String> token = CommonUtil.redisJsonToMap(redisUtil.getData(simpSessionId));
        UserChatSession userChatSession = UserChatSession.builder()
                .chatRoomId(roomId)
                .userId(Long.valueOf(token.get("id")))
                .userAccount(token.get("account"))
                .userName(token.get("name"))
                .simpSessionId(simpSessionId)
                .build();
        UserChatSessions.add(userChatSession);
    }

    public void removeUserChatSession(String simpSessionId) {
        UserChatSessions.removeIf(userChatSession -> userChatSession.getSimpSessionId().equals(simpSessionId));
    }

    public void userChatRoomSessionSynchroization(Long chatRoomId) throws JsonProcessingException {
        List<Map<String, Object>> userChatRoomSessions = new ArrayList<>();
        UserChatSessions.stream().filter(userChatSession -> userChatSession.getChatRoomId().equals(chatRoomId)).forEach(userChatSession -> {
            Map<String, Object> userChatRoomSession = new HashMap<>();
            userChatRoomSession.put("userId", userChatSession.getUserId());
            userChatRoomSession.put("userAccount", userChatSession.getUserAccount());
            userChatRoomSession.put("userName", userChatSession.getUserName());
            userChatRoomSession.put("simpSessionId", userChatSession.getSimpSessionId());
            userChatRoomSessions.add(userChatRoomSession);
        });
        String message = CommonUtil.objectToJsonString(userChatRoomSessions);
        template.convertAndSend("/sub/user/chatroom/" + chatRoomId, message);
    }

    public String notificationMessageMapping(String messageContent) throws JsonProcessingException {
        Map<String, Object> messageMapping = new HashMap<>();
        messageMapping.put("userName", "[알림]");
        messageMapping.put("messageContent", messageContent);
        messageMapping.put("date", CommonUtil.getLocalTime());
        messageMapping.put("authority", "notice");
        return CommonUtil.objectToJsonString(messageMapping);
    }

    public UserChatSession getSessionUser(String simpSessionId) {
        return UserChatSessions.stream()
                .filter(userChatSession -> userChatSession.getSimpSessionId().equals(simpSessionId))
                .findFirst().get();
    }

}
