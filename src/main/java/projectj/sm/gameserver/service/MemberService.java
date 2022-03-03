package projectj.sm.gameserver.service;

import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.dto.MemberDto;
import projectj.sm.gameserver.security.PasswordAuthAuthenticationToken;

import java.util.HashMap;
import java.util.List;

public interface MemberService {
    PasswordAuthAuthenticationToken passwordAuth(String account, String password);
    List<Member> getMemberList();
    String getKakaoAccessToken(String code);
    HashMap<String, Object> getKakaoUserInfo(String accessToken);
    void memberSave(MemberDto dto);
}
