package projectj.sm.gameserver.graphql.game.yahtzee;

import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import projectj.sm.gameserver.service.YahtzeeService;
import projectj.sm.gameserver.vo.YahtzeeGameRankVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
@Component
@RequiredArgsConstructor
@Transactional
public class YahtzeeQuery implements GraphQLQueryResolver {
    private final YahtzeeService yahtzeeService;

    public List<YahtzeeGameRankVo> getYahtzeeGameRank() {
        return yahtzeeService.getYahtzeeGameRank();
    }
}
