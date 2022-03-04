package projectj.sm.gameserver.controller.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.RedisUtil;
import projectj.sm.gameserver.vo.session.UserChatSession;

@Log
@RestController
@RequiredArgsConstructor
public class SubscribeEvent {

    private final RedisUtil redisUtil;

//    private final SimpMessagingTemplate template;
//
//    @EventListener
//    public void sessionSubscribeEvent(SessionSubscribeEvent event) throws JsonProcessingException {
//        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
//        String subscribeAddress = CommonUtil.extractDataFromEventMessages(event, "destination");
//
//        if (subscribeAddress.contains("/sub/yahtzee/score/")) {
//            redisUtil.setData("sub/yahtzee/score/" + simpSessionId, subscribeAddress);
//        }
//        if (subscribeAddress.contains("/sub/chatroom/list/")) {
//            updateChatRoomListAll();
//        }
//
//        if (subscribeAddress.contains("/sub/chatting/chatroom/")) {
//            redisUtil.setData("/sub/chatting/chatroom/" + simpSessionId, subscribeAddress);
//            Long chatRoomId = Long.parseLong(subscribeAddress.split("/sub/chatting/chatroom/")[1]);
//            createUserChatSession(chatRoomId, simpSessionId);
//            userChatRoomSessionSynchroization(chatRoomId);
//            UserChatSession userInfo = getSessionUser(simpSessionId);
//
//            String message = notificationMessageMapping(userInfo.getUserName() + "님이 채팅방에 입장하였습니다.");
//            template.convertAndSend("/sub/chatting/chatroom/" + chatRoomId, message);
//        }
//    }
}
