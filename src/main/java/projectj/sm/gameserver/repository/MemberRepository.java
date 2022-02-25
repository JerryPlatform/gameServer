package projectj.sm.gameserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projectj.sm.gameserver.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
