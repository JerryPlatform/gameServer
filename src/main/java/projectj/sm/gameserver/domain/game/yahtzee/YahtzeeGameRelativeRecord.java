package projectj.sm.gameserver.domain.game.yahtzee;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import projectj.sm.gameserver.domain.Member;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class YahtzeeGameRelativeRecord {
    @Id
    @GeneratedValue(generator = "game_gen")
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.PROTECTED)
    @CreationTimestamp
    private Date regDt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member victor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member loser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private YahtzeeGameResult gameResult;
}
