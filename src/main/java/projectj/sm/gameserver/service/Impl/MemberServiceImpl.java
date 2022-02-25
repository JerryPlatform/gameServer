package projectj.sm.gameserver.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.repository.MemberRepository;
import projectj.sm.gameserver.security.PasswordAuthAuthenticationToken;
import projectj.sm.gameserver.service.MemberService;

import java.util.List;

@Log
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;

    @Override
    public PasswordAuthAuthenticationToken passwordAuth(String account, String password) {
        log.info("1★");
        PasswordAuthAuthenticationToken token = new PasswordAuthAuthenticationToken(account, password);
        log.info("2★");
        Authentication authentication = authenticationManager.authenticate(token);
        log.info("3★");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("4★");
        return (PasswordAuthAuthenticationToken) authentication;
    }

    @Override
    public List<Member> getMemberList() {
        return memberRepository.findAll();
    }
}
