package projectj.sm.gameserver.controller.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.RedisUtil;
import projectj.sm.gameserver.domain.chat.Room;
import projectj.sm.gameserver.dto.game.yahtzee.ExpectedScoreDto;
import projectj.sm.gameserver.dto.game.yahtzee.UpdateScoreDto;
import projectj.sm.gameserver.service.ChatRoomService;
import projectj.sm.gameserver.service.GameService;
import projectj.sm.gameserver.service.YahtzeeService;
import projectj.sm.gameserver.vo.YahtzeeGameRankVo;
import projectj.sm.gameserver.vo.session.YahtzeeGameSession;

import java.util.*;

@Log
@RestController
@EnableScheduling
@RequiredArgsConstructor
public class YahtzeeController {
    private final RedisUtil redisUtil;
    private final SimpMessagingTemplate template;
    private final ChatRoomService chatRoomService;
    private final YahtzeeService yahtzeeService;
    private final GameService gameService;

    public static List<YahtzeeGameSession> yahtzeeGameSessions = new ArrayList<>();

    @MessageMapping("/update/score/yahtzee")
    public void updateYahtzeeGameScore(UpdateScoreDto dto, @Header("simpSessionId") String simpSessionId) throws JsonProcessingException {
        YahtzeeGameSession yahtzeeGameSession = yahtzeeService.getYahtzeeGameSession(dto.getRoomId());
        for (YahtzeeGameSession.userInfo userInfo : yahtzeeGameSession.getUserInfos()) {
            if (userInfo.getSimpSessionId().equals(dto.getSimpSessionId())) {
                yahtzeeGameSession.setRemainingTurns(yahtzeeGameSession.getRemainingTurns() - 1);
                YahtzeeGameSession.userInfo turnUser = yahtzeeGameSession.getUserInfos()
                        .get(yahtzeeGameSession.getRemainingTurns() % yahtzeeGameSession.getUserCount());
                yahtzeeGameSession.setTurnUserId(turnUser.getUserId());
                yahtzeeGameSession.setTurnUserName(turnUser.getUserName());
                scoreInsert(userInfo, dto.getScoreType(), dto.getScore());
                yahtzeeService.gameScoreTransfer(dto.getRoomId());
            }
        }
    }

    @PostMapping("/expected/score")
    public YahtzeeGameSession.userInfo getExpectedScore(@RequestBody ExpectedScoreDto dto) {
        return yahtzeeService.getExpectedScore(dto);
    }

    @GetMapping("/rank/yahtzee")
    public List<YahtzeeGameRankVo> getYahtzeeGameRank() {
        return yahtzeeService.getYahtzeeGameRank();
    }

    @EventListener
    public void sessionSubscribeEvent(SessionSubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String simpSubscriptionId = event.getMessage().getHeaders().get("simpSubscriptionId").toString();
        String subscribeAddress = CommonUtil.extractDataFromEventMessages(event, "destination");
        String redisKey = simpSessionId + "/" + simpSubscriptionId;

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

    @EventListener
    public void sessionDisconnectEvent(SessionDisconnectEvent event) throws JsonProcessingException {
        Set<String> keys = redisUtil.getFindKeys(event.getSessionId());
        for (String key : keys) {
            String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
            String subscribeAddress = redisUtil.getData(key);

            if (subscribeAddress != null && subscribeAddress.contains("/sub/yahtzee/score/")) {
                subYahtzeeScoreUnsubscribeOrDisconnectProcess(simpSessionId, subscribeAddress);
            }
        }
    }

    @EventListener
    public void sessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String simpSubscriptionId = event.getMessage().getHeaders().get("simpSubscriptionId").toString();
        String redisKey = simpSessionId + "/" + simpSubscriptionId;
        String subscribeAddress = redisUtil.getData(redisKey);

        if (subscribeAddress != null && subscribeAddress.contains("/sub/yahtzee/score/")) {
            subYahtzeeScoreUnsubscribeOrDisconnectProcess(simpSessionId, subscribeAddress);
        }
    }

    public void subYahtzeeScoreUnsubscribeOrDisconnectProcess(String simpSessionId, String subscribeAddress) throws JsonProcessingException {
        Integer roomId = Integer.valueOf(subscribeAddress.split("/sub/yahtzee/score/")[1]);
        Room.Status roomStatus = chatRoomService.findByChatRoomStatus(Long.valueOf(roomId));
        if (roomStatus.equals(Room.Status.PROCEEDING)) {
            YahtzeeGameSession session = yahtzeeGameSessions.stream()
                    .filter(yahtzeeGameSession -> yahtzeeGameSession.getRoomId().equals(roomId))
                    .findFirst().get();

            YahtzeeGameSession.userInfo roomOutUser = session.getUserInfos().stream()
                    .filter(userInfo -> userInfo.getSimpSessionId().equals(simpSessionId))
                    .findFirst().get();

            for (int i = 1; i <= session.getRemainingTurns(); i++) {
                if ((i % session.getUserCount()) == roomOutUser.getPlayerCount()) {
                    session.setRemainingTurns(session.getRemainingTurns() - 1);
                }
            }
            session.getUserInfos().removeIf(userInfo -> userInfo.equals(roomOutUser));
            session.setUserCount(session.getUserInfos().size());
            Room.Type type = chatRoomService.findByChatRoom(Long.valueOf(roomId)).getType();
            if (session.getUserInfos().size() == 0) {
                gameService.reflectionOfYahtzeeGameResults(Long.valueOf(roomId));
                gameService.changeRoomStatusByEndingTheGame(Long.valueOf(roomId));
                chatRoomService.updateChatRoomList(type);
                gameService.removeYahtzeeGameSession(Long.valueOf(roomId));
            } else {
                chatRoomService.updateChatRoomList(type);
                int turn = session.getRemainingTurns() % session.getUserCount();
                session.setTurnUserId(session.getUserInfos().get(turn).getUserId());
                session.setTurnUserName(session.getUserInfos().get(turn).getUserName());
                yahtzeeService.gameScoreTransfer(Long.valueOf(roomId));
            }
        }
    }

    public void subYahtzeeScoreUnSubscribeOrDisconnectProcess() {
        
    }

    public void scoreInsert(YahtzeeGameSession.userInfo userInfo, String scoreType, Integer score) {
        switch (scoreType) {
            case "ones":
                userInfo.setOnes(score);
                userInfo.setGeneralScoreTotal(userInfo.getGeneralScoreTotal() + score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                if (bonusScoreCheck(userInfo)) {
                    userInfo.setBonus(35);
                    userInfo.setTotalScore(userInfo.getTotalScore() + 35);
                }
                break;
            case "twos":
                userInfo.setTwos(score);
                userInfo.setGeneralScoreTotal(userInfo.getGeneralScoreTotal() + score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                if (bonusScoreCheck(userInfo)) {
                    userInfo.setBonus(35);
                    userInfo.setTotalScore(userInfo.getTotalScore() + 35);
                }
                break;
            case "threes":
                userInfo.setThrees(score);
                userInfo.setGeneralScoreTotal(userInfo.getGeneralScoreTotal() + score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                if (bonusScoreCheck(userInfo)) {
                    userInfo.setBonus(35);
                    userInfo.setTotalScore(userInfo.getTotalScore() + 35);
                }
                break;
            case "fours":
                userInfo.setFours(score);
                userInfo.setGeneralScoreTotal(userInfo.getGeneralScoreTotal() + score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                if (bonusScoreCheck(userInfo)) {
                    userInfo.setBonus(35);
                    userInfo.setTotalScore(userInfo.getTotalScore() + 35);
                }
                break;
            case "fives":
                userInfo.setFives(score);
                userInfo.setGeneralScoreTotal(userInfo.getGeneralScoreTotal() + score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                if (bonusScoreCheck(userInfo)) {
                    userInfo.setBonus(35);
                    userInfo.setTotalScore(userInfo.getTotalScore() + 35);
                }
                break;
            case "sixes":
                userInfo.setSixes(score);
                userInfo.setGeneralScoreTotal(userInfo.getGeneralScoreTotal() + score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                if (bonusScoreCheck(userInfo)) {
                    userInfo.setBonus(35);
                    userInfo.setTotalScore(userInfo.getTotalScore() + 35);
                }
                break;
            case "fourOfKind":
                userInfo.setFourOfKind(score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                break;
            case "fullHouse":
                userInfo.setFullHouse(score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                break;
            case "smallStraight":
                userInfo.setSmallStraight(score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                break;
            case "largeStraight":
                userInfo.setLargeStraight(score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                break;
            case "chance":
                userInfo.setChance(score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                break;
            case "yahtzee":
                userInfo.setYahtzee(score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
                break;
        }
    }

    public boolean bonusScoreCheck(YahtzeeGameSession.userInfo userInfo) {
        Integer score = userInfo.getOnes() + userInfo.getTwos() + userInfo.getThrees()
                + userInfo.getFours() + userInfo.getFives() + userInfo.getSixes();
        if (score >= 63) {
            return true;
        } else {
            return false;
        }
    }
}
