package projectj.sm.gameserver.service.Impl;

import projectj.sm.gameserver.domain.ChatRoom;
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
    public List<ChatRoom> getChatRoomAllList() {
        return chatRoomRepository.findAll();
    }

    @Override
    public ChatRoom findByChatRoom(Long id) {
        return chatRoomRepository.getById(id);
    }

    @Override
    public List<ChatRoom> getChatRoomListByType(ChatRoom.Type type) {
        return chatRoomRepository.getChatRoomListByType(type);
    }

    @Transactional
    @Override
    public void createChatRoom(ChatRoomDto dto) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(dto.getRoomName());
        chatRoom.setPassword(dto.getPassword());
        chatRoom.setType(dto.getType());
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    @Override
    public void removeChatRoom(Long id) {
        chatRoomRepository.deleteById(id);
    }

    @Override
    public Boolean SecretChatRoomVerification(SecretChatRoomVerificationDto dto) {
        ChatRoom chatRoom = chatRoomRepository.getById(dto.getChatRoomId());
        return chatRoom.getPassword().equals(dto.getPassword());
    }
}
