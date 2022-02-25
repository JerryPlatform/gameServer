package projectj.sm.gameserver.dto;

import lombok.Getter;
import lombok.Setter;
import projectj.sm.gameserver.domain.ChatRoom;

@Getter
@Setter
public class ChatRoomDto {
    private String roomName;
    private String password;
    private ChatRoom.Type type;
}
