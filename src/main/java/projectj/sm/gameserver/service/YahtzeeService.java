package projectj.sm.gameserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import projectj.sm.gameserver.dto.game.yahtzee.ExpectedScoreDto;
import projectj.sm.gameserver.vo.session.YahtzeeGameSession;

public interface YahtzeeService {
    YahtzeeGameSession getYahtzeeGameSession(Long roomId);
    void gameScoreTransfer(Long roomId) throws JsonProcessingException;
    YahtzeeGameSession.userInfo getExpectedScore(ExpectedScoreDto dto);
}
