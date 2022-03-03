package projectj.sm.gameserver.dto.game.yahtzee;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateScoreDto {
    private Long roomId;
    private String simpSessionId;
    private String scoreType;
    private Integer score;
    private Integer[] dices;
}
