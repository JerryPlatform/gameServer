package projectj.sm.gameserver.service;

import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.security.PasswordAuthAuthenticationToken;

import java.util.List;

public interface MemberService {
    PasswordAuthAuthenticationToken passwordAuth(String account, String password);
    List<Member> getMemberList();
}
