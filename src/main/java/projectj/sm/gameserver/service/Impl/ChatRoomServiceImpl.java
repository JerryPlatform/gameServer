package projectj.sm.gameserver.service.Impl;

import projectj.sm.gameserver.domain.Room;
import projectj.sm.gameserver.dto.ChatRoomDto;
import projectj.sm.gameserver.dto.SecretChatRoomVerificationDto;
import projectj.sm.gameserver.repository.ChatRoomRepository;
import projectj.sm.gameserver.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public List<Room> getChatRoomAllList() {
        return chatRoomRepository.findAll();
    }

    @Override
    public Room findByChatRoom(Long id) {
        return chatRoomRepository.getById(id);
    }

    @Override
    public List<Room> getChatRoomListByType(Room.Type type) {
        return chatRoomRepository.getChatRoomListByType(type);
    }

    @Transactional
    @Override
    public void createChatRoom(ChatRoomDto dto) {
        Room room = new Room();
        room.setRoomName(dto.getRoomName());
        room.setPassword(dto.getPassword());
        room.setType(dto.getType());
        chatRoomRepository.save(room);
    }

    @Transactional
    @Override
    public void removeChatRoom(Long id) {
        chatRoomRepository.deleteById(id);
    }

    @Override
    public Boolean SecretChatRoomVerification(SecretChatRoomVerificationDto dto) {
        Room room = chatRoomRepository.getById(dto.getChatRoomId());
        return room.getPassword().equals(dto.getPassword());
    }
}
