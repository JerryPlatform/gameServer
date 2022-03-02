package projectj.sm.gameserver.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(generator = "chat_gen")
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    private String roomName;

    private String password;

    private Type type;

    public enum Type implements Meta {
        GENERAL("일반"),
        DICEGAME("야추다이스"),
        ;

        private String desc;

        Type(String desc) { this.desc = desc; }

        @Override
        public String getDescription() { return desc; }
    }
}
