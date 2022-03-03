package projectj.sm.gameserver.dto.chat;

import lombok.Getter;
import lombok.Setter;
import projectj.sm.gameserver.domain.chat.Room;

@Getter
@Setter
public class ChatRoomDto {
    private String roomName;
    private String password;
    private Room.Type type;
}
