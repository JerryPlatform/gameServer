package projectj.sm.gameserver.domain.game.yahtzee;

import lombok.*;
import projectj.sm.gameserver.domain.Member;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class YahtzeePlayMember {
    @Id
    @GeneratedValue(generator = "game_gen")
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    private Integer totalScore;
}
