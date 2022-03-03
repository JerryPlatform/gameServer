package projectj.sm.gameserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import projectj.sm.gameserver.dto.game.yahtzee.ExpectedScoreDto;
import projectj.sm.gameserver.vo.YahtzeeGameRankVo;
import projectj.sm.gameserver.vo.session.YahtzeeGameSession;

import java.util.List;

public interface YahtzeeService {
    YahtzeeGameSession getYahtzeeGameSession(Long roomId);
    void gameScoreTransfer(Long roomId) throws JsonProcessingException;
    YahtzeeGameSession.userInfo getExpectedScore(ExpectedScoreDto dto);
    List<YahtzeeGameRankVo> getYahtzeeGameRank();
}
