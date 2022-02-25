package projectj.sm.gameserver.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Getter @Setter
public class Member {
    @Id
    @GeneratedValue(generator = "common_gen")
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String account;

    @Column(nullable = false)
    private String password;

    private boolean valid;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Getter
    public enum Role implements Meta{
        ROLE_ADMIN("관리자"),
        ROLE_USER("사용자"),
        ROLE_ANONYMOUS("알수없음");

        private String desc;

        Role(String desc) {
            this.desc = desc;
        }

        public static Role of(String code) {
            return Arrays.stream(Role.values()).filter(r -> r.name().equals(code)).findAny().orElse(ROLE_ANONYMOUS);
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }
}
