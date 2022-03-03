package projectj.sm.gameserver.domain.game.yahtzee;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import projectj.sm.gameserver.domain.Member;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@Getter @Setter @AllArgsConstructor
@Builder
public class YahtzeeGameResult {
    @Id
    @GeneratedValue(generator = "game_gen")
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.PROTECTED)
    @CreationTimestamp
    private Date regDt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "yahtzee_result_id", nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private List<YahtzeePlayMember> yahtzeeGamePlayMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="winner_id")
    private Member winner;

    private Integer totalScore;

    public YahtzeeGameResult() {
        yahtzeeGamePlayMember = new ArrayList<>();
    }

    public void addYahtzeeGamePlayMember(YahtzeePlayMember yahtzeePlayMember) {
        this.yahtzeeGamePlayMember.add(yahtzeePlayMember);
    }
}
