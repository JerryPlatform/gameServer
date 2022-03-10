package projectj.sm.gameserver.controller.socketEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import projectj.sm.gameserver.RedisUtil;
import projectj.sm.gameserver.controller.chat.ChatController;
import projectj.sm.gameserver.controller.game.YahtzeeController;

@Log
@RestController
@EnableScheduling
@RequiredArgsConstructor
public class sessionUnsubscribeEvent {

    private final ChatController chatController;

    private final YahtzeeController yahtzeeController;

    private final RedisUtil redisUtil;

    @EventListener
    public void sessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String simpSubscriptionId = event.getMessage().getHeaders().get("simpSubscriptionId").toString();
        String redisKey = simpSessionId + "/" + simpSubscriptionId;
        String subscribeAddress = redisUtil.getData(redisKey);

        if (subscribeAddress != null && subscribeAddress.contains("/sub/chatroom/list/")) {
            redisUtil.deleteData(redisKey);
            chatController.updateChatRoomListAll();
        }
        if (subscribeAddress != null && subscribeAddress.contains("/sub/chatting/chatroom/")) {
            chatController.subChattingChatroomUnsubscribeOrDisconnectProcess(simpSessionId, redisKey, subscribeAddress);
        }

        if (subscribeAddress != null && subscribeAddress.contains("/sub/yahtzee/score/")) {
            yahtzeeController.subYahtzeeScoreUnsubscribeOrDisconnectProcess(simpSessionId, subscribeAddress);
        }
    }
}
