package projectj.sm.gameserver.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import projectj.sm.gameserver.CommonUtil;
import projectj.sm.gameserver.domain.chat.Room;
import projectj.sm.gameserver.dto.chat.ChatRoomDto;
import projectj.sm.gameserver.dto.chat.SecretChatRoomVerificationDto;
import projectj.sm.gameserver.repository.chat.ChatRoomRepository;
import projectj.sm.gameserver.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static projectj.sm.gameserver.controller.chat.ChatController.userChatSessions;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private final SimpMessagingTemplate template;

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
        room.setStatus(Room.Status.GENERAL);
        chatRoomRepository.save(room);
    }

    @Transactional
    @Override
    public void removeChatRoom(Long id) {
        chatRoomRepository.deleteById(id);
    }

    @Override
    public void updateChatRoomList(Room.Type type) throws JsonProcessingException {
        List<Room> roomList = getChatRoomListByType(type);
        List<Map<String, Object>> chatRoomInfos = new ArrayList<>();

        for (Room room : roomList) {
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("chatRoomId", room.getId());
            roomInfo.put("chatRoomName", room.getRoomName());
            roomInfo.put("userCount", userChatSessions.stream().filter(userChatSession -> userChatSession.getChatRoomId().equals(room.getId())).count());
            roomInfo.put("private", room.getPassword() != null ? true : false);
            roomInfo.put("status", room.getStatus().getDescription());
            chatRoomInfos.add(roomInfo);
        }

        String resultValue = CommonUtil.objectToJsonString(chatRoomInfos);
        template.convertAndSend("/sub/chatroom/list/" + type, resultValue);
    }

    @Override
    public Boolean SecretChatRoomVerification(SecretChatRoomVerificationDto dto) {
        Room room = chatRoomRepository.getById(dto.getChatRoomId());
        return room.getPassword().equals(dto.getPassword());
    }
}
