package projectj.sm.gameserver.repository.game.yahtzee;

import org.springframework.data.jpa.repository.JpaRepository;
import projectj.sm.gameserver.domain.game.yahtzee.YahtzeeGameResult;

public interface YahtzeeGameResultRepository extends JpaRepository<YahtzeeGameResult, Long> {
}
