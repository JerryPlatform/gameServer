package projectj.sm.gameserver.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ChatRoom {
    @Id
    @GeneratedValue(generator = "chat_gen")
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    private String roomName;

    private String password;
}
