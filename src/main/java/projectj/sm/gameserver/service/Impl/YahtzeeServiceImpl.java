package projectj.sm.gameserver.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.dto.game.yahtzee.ExpectedScoreDto;
import projectj.sm.gameserver.service.YahtzeeService;
import projectj.sm.gameserver.vo.session.YahtzeeGameSession;

import java.util.Arrays;

import static projectj.sm.gameserver.controller.game.YahtzeeController.yahtzeeGameSessions;

@Log
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class YahtzeeServiceImpl implements YahtzeeService {

    private final SimpMessagingTemplate template;

    @Override
    public YahtzeeGameSession getYahtzeeGameSession(Long roomId) {
        return yahtzeeGameSessions.stream()
                .filter(yahtzeeGameSession -> yahtzeeGameSession.getRoomId().equals(roomId))
                .findFirst().get();
    }

    @Override
    public void gameScoreTransfer(Long roomId) throws JsonProcessingException {
        YahtzeeGameSession session = yahtzeeGameSessions.stream()
                .filter(yahtzeeGameSession -> yahtzeeGameSession.getRoomId().equals(roomId))
                .findFirst().get();
        String message = CommonUtil.objectToJsonString(session);
        template.convertAndSend("/sub/yahtzee/score/" + roomId, message);
    }

    @Override
    public YahtzeeGameSession.userInfo getExpectedScore(ExpectedScoreDto dto) {
        Integer[] diceCount = getDiceCount(dto.getDices());

        return YahtzeeGameSession.userInfo.builder()
                .aces(getScore(diceCount, "aces"))
                .twos(getScore(diceCount,"twos"))
                .threes(getScore(diceCount,"threes"))
                .fours(getScore(diceCount,"fours"))
                .fives(getScore(diceCount,"fives"))
                .sixes(getScore(diceCount,"sixes"))
                .threeOfKind(getScore(diceCount,"threeOfKind"))
                .fourOfKind(getScore(diceCount,"fourOfKind"))
                .fullHouse(getScore(diceCount,"fullHouse"))
                .smallStraight(getScore(diceCount,"smallStraight"))
                .largeStraight(getScore(diceCount,"largeStraight"))
                .chance(getScore(diceCount,"chance"))
                .yahtzee(getScore(diceCount,"yahtzee"))
                .build();
    }

    public static Integer getScore(Integer[] diceCount, String scoreType) {
        switch (scoreType) {
            case "aces":
                return diceCount[0] * 1;
            case "twos":
                return diceCount[1] * 2;
            case "threes":
                return diceCount[2] * 3;
            case "fours":
                return diceCount[3] * 4;
            case "fives":
                return diceCount[4] * 5;
            case "sixes":
                return diceCount[5] * 6;
            case "threeOfKind":
                for (Integer i : diceCount) {
                    if (i.equals(3)) {
                        return
                        (diceCount[0] * 1) + (diceCount[1] * 2) + (diceCount[2] * 3) +
                        (diceCount[3] * 4) + (diceCount[4] * 5) + (diceCount[5] * 6);
                    }
                }
                break;
            case "fourOfKind":
                for (Integer i : diceCount) {
                    if (i.equals(4)) {
                        return
                        (diceCount[0] * 1) + (diceCount[1] * 2) + (diceCount[2] * 3) +
                        (diceCount[3] * 4) + (diceCount[4] * 5) + (diceCount[5] * 6);
                    } else { return 0; }
                }
                break;
            case "fullHouse":
                boolean check1 = false;
                boolean check2 = false;
                for (Integer i : diceCount) {
                    if (i.equals(2)) { check1 = true; }
                    if (i.equals(3)) { check2 = true; }
                }
                if (check1 && check2) {
                    return 25;
                } else {
                    return 0;
                }
            case "smallStraight":
                if (diceCount[2] >= 1 && diceCount[3] >= 1) {
                    if ((diceCount[0] >= 1 && diceCount[1] >= 1) ||
                        (diceCount[1] >= 1 && diceCount[4] >= 1) ||
                        (diceCount[4] >= 1 && diceCount[5] >= 1)) {
                        return 30;
                    } else { return 0; }
                } else { return 0; }
            case "largeStraight":
                if (diceCount[1] >= 1 && diceCount[2] >= 1 && diceCount[3] >= 1 && diceCount[4] >= 1) {
                    if (diceCount[0] >= 1 || diceCount[5] >= 1) {
                        return 40;
                    } else { return 0; }
                } else { return 0; }
            case "chance":
                return
                        (diceCount[0] * 1) + (diceCount[1] * 2) + (diceCount[2] * 3) +
                        (diceCount[3] * 4) + (diceCount[4] * 5) + (diceCount[5] * 6);
            case "yahtzee":
                for (Integer i : diceCount) {
                    if (i.equals(5)) {
                        return 50;
                    }
                }
                return 0;
        }
        return null;
    }

    public static Integer[] getDiceCount(Integer[] dices) {
        Integer[] dicesCount = new Integer[6];
        Arrays.fill(dicesCount, 0);
        for (Integer i : dices) {
            dicesCount[i-1]++;
        }
        return dicesCount;
    }
}
