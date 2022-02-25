package projectj.sm.gameserver.service;

import projectj.sm.gameserver.domain.ChatRoom;
import projectj.sm.gameserver.dto.ChatRoomDto;
import projectj.sm.gameserver.dto.SecretChatRoomVerificationDto;

import java.util.List;

public interface ChatRoomService {
    List<ChatRoom> getChatRoomAllList();
    ChatRoom findByChatRoom(Long id);
    List<ChatRoom> getChatRoomListByType(ChatRoom.Type type);
    void createChatRoom(ChatRoomDto dto);
    void removeChatRoom(Long id);
    Boolean SecretChatRoomVerification(SecretChatRoomVerificationDto dto);
}
