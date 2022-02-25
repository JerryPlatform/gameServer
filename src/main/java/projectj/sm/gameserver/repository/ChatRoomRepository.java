package projectj.sm.gameserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import projectj.sm.gameserver.domain.ChatRoom;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("select chatroom from ChatRoom chatroom where chatroom.type = :type ")
    List<ChatRoom> getChatRoomListByType(ChatRoom.Type type);
}
