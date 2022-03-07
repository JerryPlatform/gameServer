package projectj.sm.gameserver.service;

import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.dto.MemberDto;
import projectj.sm.gameserver.dto.MemberUseDto;
import projectj.sm.gameserver.security.PasswordAuthAuthenticationToken;
import projectj.sm.gameserver.vo.MemberVo;
import projectj.sm.gameserver.vo.Result;

import java.util.HashMap;
import java.util.List;

public interface MemberService {
    PasswordAuthAuthenticationToken passwordAuth(String account, String password) throws Exception;
    List<Member> getMemberList();
    String getKakaoAccessToken(String code);
    void memberSave(MemberDto dto);
    MemberVo temporaryMemberIssuance(MemberUseDto dto);
    Result getNewToken();
}
