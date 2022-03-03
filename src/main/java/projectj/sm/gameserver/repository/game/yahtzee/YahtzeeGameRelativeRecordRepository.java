package projectj.sm.gameserver.repository.game.yahtzee;

import org.springframework.data.jpa.repository.JpaRepository;
import projectj.sm.gameserver.domain.game.yahtzee.YahtzeeGameRelativeRecord;

public interface YahtzeeGameRelativeRecordRepository extends JpaRepository<YahtzeeGameRelativeRecord, Long> {
}
