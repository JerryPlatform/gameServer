package projectj.sm.gameserver.service;

import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.dto.MemberDto;
import projectj.sm.gameserver.dto.MemberUseDto;
import projectj.sm.gameserver.security.PasswordAuthAuthenticationToken;

import java.util.HashMap;
import java.util.List;

public interface MemberService {
    PasswordAuthAuthenticationToken passwordAuth(String account, String password);
    List<Member> getMemberList();
    String getKakaoAccessToken(String code);
    void memberSave(MemberDto dto);
    boolean memberUse(MemberUseDto dto);
}
