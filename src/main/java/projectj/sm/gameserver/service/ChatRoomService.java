package projectj.sm.gameserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import projectj.sm.gameserver.domain.chat.Room;
import projectj.sm.gameserver.dto.chat.ChatRoomDto;
import projectj.sm.gameserver.dto.chat.SecretChatRoomVerificationDto;

import java.util.List;

public interface ChatRoomService {
    List<Room> getChatRoomAllList();
    Room findByChatRoom(Long id);
    Room.Status findByChatRoomStatus(Long id);
    List<Room> getChatRoomListByType(Room.Type type);
    void createChatRoom(ChatRoomDto dto);
    void removeChatRoom(Long id);
    void updateChatRoomList(Room.Type type) throws JsonProcessingException;
    Boolean SecretChatRoomVerification(SecretChatRoomVerificationDto dto);
}
