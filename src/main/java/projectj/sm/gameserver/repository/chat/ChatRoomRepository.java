package projectj.sm.gameserver.repository.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import projectj.sm.gameserver.domain.chat.Room;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<Room, Long> {
    @Query("select room from Room room where room.type = :type ")
    List<Room> getChatRoomListByType(Room.Type type);

    @Modifying
    @Query("update Room r set r.status = :status where r.id = :id ")
    void changeRoomStatus(Long id, Room.Status status);
}
