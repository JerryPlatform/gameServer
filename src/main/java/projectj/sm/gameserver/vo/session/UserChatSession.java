package projectj.sm.gameserver.vo.session;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserChatSession {
    private Long chatRoomId;
    private Long userId;
    private String userAccount;
    private String userName;
    private String simpSessionId;
}
