package projectj.sm.gameserver.controller.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.domain.chat.Room;
import projectj.sm.gameserver.service.ChatRoomService;
import projectj.sm.gameserver.service.GameService;
import projectj.sm.gameserver.service.YahtzeeService;
import projectj.sm.gameserver.vo.session.YahtzeeGameSession;

import static projectj.sm.gameserver.controller.game.YahtzeeController.yahtzeeGameSessions;

@Log
@RestController
@EnableScheduling
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    private final YahtzeeService yahtzeeService;

    private final ChatRoomService chatRoomService;

    private final SimpMessagingTemplate template;

    @PutMapping("/room/{roomId}/start")
    public void changeRoomStatusByStartingTheGame(@PathVariable Long roomId) throws JsonProcessingException {
        Room.Type type = chatRoomService.findByChatRoom(roomId).getType();
        gameService.changeRoomStatusByStartingTheGame(roomId);
        chatRoomService.updateChatRoomList(type);
        createGameSession(roomId);
    }

    @PutMapping("/room/{roomId}/end")
    public void changeRoomStatusByEndingTheGame(@PathVariable Long roomId) throws JsonProcessingException {
        Room.Type type = chatRoomService.findByChatRoom(roomId).getType();
        reflectionOfGameResults(roomId);
        gameService.changeRoomStatusByEndingTheGame(roomId);
        chatRoomService.updateChatRoomList(type);
        removeGameSession(roomId);
    }

    public void createGameSession(Long roomId) throws JsonProcessingException {
        Room.Type type = chatRoomService.findByChatRoom(roomId).getType();
        switch (type) {
            case YAHTZEE:
                gameService.createYahtzeeGameSession(roomId);
                YahtzeeGameSession session = yahtzeeGameSessions.stream()
                        .filter(yahtzeeGameSession -> yahtzeeGameSession.getRoomId().equals(roomId))
                        .findFirst().get();
                String message = CommonUtil.objectToJsonString(session);
                template.convertAndSend("/sub/yahtzee/score/" + roomId, message);
                break;
        }
    }

    public void removeGameSession(Long roomId) {
        Room.Type type = chatRoomService.findByChatRoom(roomId).getType();
        switch (type) {
            case YAHTZEE:
                gameService.removeYahtzeeGameSession(roomId);
                break;
        }
    }

    public void reflectionOfGameResults(Long roomId) {
        Room.Type type = chatRoomService.findByChatRoom(roomId).getType();
        switch (type) {
            case YAHTZEE:
                gameService.reflectionOfYahtzeeGameResults(roomId);
                break;
        }
    }
}
