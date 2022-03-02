package projectj.sm.gameserver.controller.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.RedisUtil;
import projectj.sm.gameserver.domain.ChatRoom;
import projectj.sm.gameserver.dto.ChatRoomDto;
import projectj.sm.gameserver.dto.ChattingMessageDto;
import projectj.sm.gameserver.dto.SecretChatRoomVerificationDto;
import projectj.sm.gameserver.service.ChatRoomService;

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

    List<Map<String, Object>> userChatSessions = new ArrayList<>();

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
        ChatRoom.Type type = chatRoomService.findByChatRoom(id).getType();
        chatRoomService.removeChatRoom(id);
        updateChatRoomList(type);
    }

    @MessageMapping("/message/chatroom")
    public void sendChattingMessage(ChattingMessageDto dto, @Header("simpSessionId") String simpSessionId) throws JsonProcessingException {
        ChatRoom chatRoom = chatRoomService.findByChatRoom(dto.getChatRoomId());
        Map<String, Object> userInfo = getSessionUser(simpSessionId);

        Map<String, Object> messageMapping = new HashMap<>();
        messageMapping.put("userName", userInfo.get("userName"));
        messageMapping.put("messageContent", dto.getMessageContent());
        messageMapping.put("date", CommonUtil.getLocalTime());
        messageMapping.put("authority", "user");

        String message = CommonUtil.objectToJsonString(messageMapping);
        template.convertAndSend("/sub/chatting/chatroom/" + chatRoom.getId(), message);
    }

    public void updateChatRoomList(ChatRoom.Type type) throws JsonProcessingException {
        List<ChatRoom> chatRoomList = chatRoomService.getChatRoomListByType(type);
        List<Map<String, Object>> chatRoomInfos = new ArrayList<>();

        for (ChatRoom chatRoom : chatRoomList) {
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("chatRoomId", chatRoom.getId());
            roomInfo.put("chatRoomName", chatRoom.getRoomName());
            roomInfo.put("userCount", userChatSessions.stream().filter(map -> map.get("chatRoomId").equals(chatRoom.getId())).count());
            roomInfo.put("private", chatRoom.getPassword() != null ? true : false);
            chatRoomInfos.add(roomInfo);
        }

        String resultValue = CommonUtil.objectToJsonString(chatRoomInfos);
        template.convertAndSend("/sub/chatroom/list/" + type, resultValue);
    }

    public void updateChatRoomListAll() throws JsonProcessingException {
        for (ChatRoom.Type type : ChatRoom.Type.values()) {
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
            Map<String, Object> userInfo = getSessionUser(simpSessionId);

            String message = notificationMessageMapping(userInfo.get("userName") + "님이 채팅방에 입장하였습니다.");
            template.convertAndSend("/sub/chatting/chatroom/" + chatRoomId, message);
        }
    }

    @EventListener
    public void SessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String subscribeAddress = CommonUtil.extractDataFromEventMessages(event, "destination");

        if (subscribeAddress.contains("/sub/chatting/chatroom/")) {
            Map<String, Object> userInfo = getSessionUser(simpSessionId);
            Long chatRoomId = Long.parseLong(subscribeAddress.split("/sub/chatting/chatroom/")[1]);
            removeUserChatSession(simpSessionId);
            userChatRoomSessionSynchroization(chatRoomId);

            String message = notificationMessageMapping(userInfo.get("userName") + "님이 채팅방에 퇴장하였습니다.");
            template.convertAndSend("/sub/chatting/chatroom/" + chatRoomId, message);
        }
    }

    public void createUserChatSession(Long roomId, String simpSessionId) throws JsonProcessingException {
        Map<String, String> token = CommonUtil.redisJsonToMap(redisUtil.getData(simpSessionId));
        Map<String, Object> userChatSession = new HashMap<>();
        userChatSession.put("chatRoomId", roomId);
        userChatSession.put("userId", token.get("id"));
        userChatSession.put("userAccount", token.get("account"));
        userChatSession.put("userName", token.get("name"));
        userChatSession.put("simpSessionId", simpSessionId);
        userChatSessions.add(userChatSession);
    }

    public void removeUserChatSession(String simpSessionId) {
        userChatSessions.removeIf(map -> map.get("simpSessionId").equals(simpSessionId));
    }

    public void userChatRoomSessionSynchroization(Long chatRoomId) throws JsonProcessingException {
        List<Map<String, Object>> userChatRoomSessions = new ArrayList<>();
        userChatSessions.stream().filter(map -> map.get("chatRoomId").equals(chatRoomId)).forEach(map -> {
            Map<String, Object> userChatRoomSession = new HashMap<>();
            userChatRoomSession.put("userId", map.get("userId"));
            userChatRoomSession.put("userAccount", map.get("userAccount"));
            userChatRoomSession.put("userName", map.get("userName"));
            userChatRoomSession.put("simpSessionId", map.get("simpSessionId"));
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

    public Map<String, Object> getSessionUser(String simpSessionId) {
        return userChatSessions.stream()
                .filter(map -> map.get("simpSessionId").equals(simpSessionId))
                .findFirst().get();
    }

}
