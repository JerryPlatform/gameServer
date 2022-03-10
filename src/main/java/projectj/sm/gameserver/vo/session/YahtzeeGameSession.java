package projectj.sm.gameserver.vo.session;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class YahtzeeGameSession {
    private Long roomId;
    private Integer userCount;
    private Integer remainingTurns;
    private String turnUserName;
    private Long turnUserId;
    private List<userInfo> userInfos;

    @Getter
    @Setter
    @Builder
    public static class userInfo {
        private String simpSessionId;
        private Long userId;
        private String userName;
        private String userAccount;
        private Integer playerCount;

        private Integer ones;
        private Integer twos;
        private Integer threes;
        private Integer fours;
        private Integer fives;
        private Integer sixes;
        private Integer generalScoreTotal;

        private Integer bonus;

        private Integer fourOfKind;
        private Integer fullHouse;
        private Integer smallStraight;
        private Integer largeStraight;
        private Integer chance;
        private Integer yahtzee;

        private Integer totalScore;
    }
}
