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
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.RedisUtil;
import projectj.sm.gameserver.dto.game.yahtzee.ExpectedScoreDto;
import projectj.sm.gameserver.dto.game.yahtzee.UpdateScoreDto;
import projectj.sm.gameserver.service.ChatRoomService;
import projectj.sm.gameserver.service.YahtzeeService;
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

    public static List<YahtzeeGameSession> yahtzeeGameSessions = new ArrayList<>();

    @MessageMapping("/update/score/yahtzee")
    public void updateYahtzeeGameScore(UpdateScoreDto dto, @Header("simpSessionId") String simpSessionId) throws JsonProcessingException {
        YahtzeeGameSession yahtzeeGameSession = yahtzeeService.getYahtzeeGameSession(dto.getRoomId());
        for (YahtzeeGameSession.userInfo userInfo : yahtzeeGameSession.getUserInfos()) {
            if (userInfo.getSimpSessionId().equals(dto.getSimpSessionId())) {
                yahtzeeGameSession.setRemainingTurns(yahtzeeGameSession.getRemainingTurns() - 1);
                yahtzeeGameSession.setTurnUserName(yahtzeeGameSession.getUserInfos()
                        .get(yahtzeeGameSession.getRemainingTurns() % yahtzeeGameSession.getUserCount()).getUserName());
                scoreInsert(userInfo, dto.getScoreType(), dto.getScore());
                yahtzeeService.gameScoreTransfer(dto.getRoomId());
            }
        }
    }

    @PostMapping("/expected/socre")
    public YahtzeeGameSession.userInfo getExpectedScore(@RequestBody ExpectedScoreDto dto) {
        return yahtzeeService.getExpectedScore(dto);
    }

    @EventListener
    public void sessionSubscribeEvent(SessionSubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String subscribeAddress = CommonUtil.extractDataFromEventMessages(event, "destination");
    }

    @EventListener
    public void SessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws JsonProcessingException {
        String simpSessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        String subscribeAddress = CommonUtil.extractDataFromEventMessages(event, "destination");
    }

    public void scoreInsert(YahtzeeGameSession.userInfo userInfo, String scoreType, Integer score) {
        switch (scoreType) {
            case "aces":
                userInfo.setAces(score);
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
            case "threeOfKind":
                userInfo.setThreeOfKind(score);
                userInfo.setTotalScore(userInfo.getTotalScore() + score);
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
        Integer score = userInfo.getAces() + userInfo.getTwos() + userInfo.getThrees()
                + userInfo.getFours() + userInfo.getFives() + userInfo.getSixes();
        if (score >= 63) {
            return true;
        } else {
            return false;
        }
    }
}
