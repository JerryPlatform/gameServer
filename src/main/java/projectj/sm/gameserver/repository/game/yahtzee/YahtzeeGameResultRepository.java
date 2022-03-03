package projectj.sm.gameserver.repository.game.yahtzee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import projectj.sm.gameserver.domain.game.yahtzee.YahtzeeGameResult;

import java.util.List;

public interface YahtzeeGameResultRepository extends JpaRepository<YahtzeeGameResult, Long> {
    List<YahtzeeGameResult> findTop10ByOrderByTotalScoreDesc();
}
