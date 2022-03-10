package projectj.sm.gameserver.controller.socketEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.RedisUtil;
import projectj.sm.gameserver.controller.chat.ChatController;
import projectj.sm.gameserver.domain.chat.Room;
import projectj.sm.gameserver.service.ChatRoomService;
import projectj.sm.gameserver.vo.session.UserChatSession;
import projectj.sm.gameserver.vo.session.YahtzeeGameSession;

import static projectj.sm.gameserver.controller.game.YahtzeeController.yahtzeeGameSessions;

@Log
@RestController
@EnableScheduling
@RequiredArgsConstructor
public class sessionSubscribeEvent {

    private final ChatController chatController;

    private final ChatRoomService chatRoomService;

    private final RedisUtil redisUtil;

    private final SimpMessagingTemplate template;

    @EventListener
    public void sessionSubscribeEvent(SessionSubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String simpSubscriptionId = event.getMessage().getHeaders().get("simpSubscriptionId").toString();
        String subscribeAddress = CommonUtil.extractDataFromEventMessages(event, "destination");
        String redisKey = simpSessionId + "/" + simpSubscriptionId;

        if (subscribeAddress.contains("/sub/chatroom/list/")) {
            redisUtil.setData(redisKey, subscribeAddress);
            chatController.updateChatRoomListAll();
        }

        if (subscribeAddress.contains("/sub/chatting/chatroom/")) {
            redisUtil.setData(redisKey, subscribeAddress);
            Long chatRoomId = Long.parseLong(subscribeAddress.split("/sub/chatting/chatroom/")[1]);
            chatController.createUserChatSession(chatRoomId, simpSessionId);
            chatController.userChatRoomSessionSynchroization(chatRoomId);
            UserChatSession userInfo = chatController.getSessionUser(simpSessionId);

            String message = chatController.notificationMessageMapping(userInfo.getUserName() + "님이 채팅방에 입장하였습니다.");
            template.convertAndSend("/sub/chatting/chatroom/" + chatRoomId, message);
        }

        if (subscribeAddress.contains("/sub/yahtzee/score/")) {
            redisUtil.setData(redisKey, subscribeAddress);

            Long roomId = Long.valueOf(subscribeAddress.split("/sub/yahtzee/score/")[1]);
            Room.Status status = chatRoomService.findByChatRoomStatus(roomId);
            if (status.equals(Room.Status.PROCEEDING)) {
                YahtzeeGameSession session = yahtzeeGameSessions.stream()
                        .filter(yahtzeeGameSession -> yahtzeeGameSession.getRoomId().equals(roomId))
                        .findFirst().get();
                String message = CommonUtil.objectToJsonString(session);
                template.convertAndSend("/sub/yahtzee/score/" + roomId, message);
            }
        }
    }
}
