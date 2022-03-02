package projectj.sm.gameserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import projectj.sm.gameserver.domain.Room;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<Room, Long> {
    @Query("select room from Room room where room.type = :type ")
    List<Room> getChatRoomListByType(Room.Type type);
}
