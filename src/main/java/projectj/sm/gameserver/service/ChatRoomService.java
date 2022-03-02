package projectj.sm.gameserver.service;

import projectj.sm.gameserver.domain.Room;
import projectj.sm.gameserver.dto.ChatRoomDto;
import projectj.sm.gameserver.dto.SecretChatRoomVerificationDto;

import java.util.List;

public interface ChatRoomService {
    List<Room> getChatRoomAllList();
    Room findByChatRoom(Long id);
    List<Room> getChatRoomListByType(Room.Type type);
    void createChatRoom(ChatRoomDto dto);
    void removeChatRoom(Long id);
    Boolean SecretChatRoomVerification(SecretChatRoomVerificationDto dto);
}
