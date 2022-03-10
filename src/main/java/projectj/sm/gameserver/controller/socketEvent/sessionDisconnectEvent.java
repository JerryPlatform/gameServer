package projectj.sm.gameserver.controller.socketEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import projectj.sm.gameserver.RedisUtil;
import projectj.sm.gameserver.controller.chat.ChatController;
import projectj.sm.gameserver.controller.game.YahtzeeController;

import java.util.Set;

@Log
@RestController
@EnableScheduling
@RequiredArgsConstructor
public class sessionDisconnectEvent {

    private final ChatController chatController;

    private final YahtzeeController yahtzeeController;

    private final RedisUtil redisUtil;

    @EventListener
    public void sessionDisconnectEvent(SessionDisconnectEvent event) throws JsonProcessingException {
        Set<String> keys = redisUtil.getFindKeys(event.getSessionId());
//        for (String key : keys) {
//            String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
//            String subscribeAddress = redisUtil.getData(key);
//
//            if (subscribeAddress != null && subscribeAddress.contains("/sub/chatroom/list/")) {
//                redisUtil.deleteData(key);
//                chatController.updateChatRoomListAll();
//            }
//            if (subscribeAddress != null && subscribeAddress.contains("/sub/chatting/chatroom/")) {
//                chatController.subChattingChatroomUnsubscribeOrDisconnectProcess(simpSessionId, key, subscribeAddress);
//            }
//
//            if (subscribeAddress != null && subscribeAddress.contains("/sub/yahtzee/score/")) {
//                yahtzeeController.subYahtzeeScoreUnsubscribeOrDisconnectProcess(simpSessionId, subscribeAddress);
//            }
//        }
    }
}
