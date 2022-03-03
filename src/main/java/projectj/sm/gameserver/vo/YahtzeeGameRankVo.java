package projectj.sm.gameserver.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class YahtzeeGameRankVo {
    private Long id;
    private String winner;
    private Integer winnerScore;
    private List<Map<String, Object>> playMembers;
}
