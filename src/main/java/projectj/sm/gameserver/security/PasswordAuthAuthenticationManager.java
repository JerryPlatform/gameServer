package projectj.sm.gameserver.security;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.exception.ErrorCode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Log
@Component
public class PasswordAuthAuthenticationManager implements AuthenticationProvider {
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpServletRequest request;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Member auth = em.createNamedQuery("findAuthWithMemberByPasswordAuth", Member.class)
                .setParameter("account", authentication.getPrincipal())
                .getResultStream().findFirst().orElseThrow(() -> new UsernameNotFoundException(ErrorCode.NOT_EXIST_SUCH_USER.getMessage()));

        if (!passwordEncoder.matches(authentication.getCredentials().toString(), auth.getPassword()))
            throw new BadCredentialsException(ErrorCode.AUTHENTICATION_FAILED.getMessage());
        else if (!auth.isValid())
            throw new LockedException(ErrorCode.LOCKED.getMessage());

        PasswordAuthAuthenticationToken resultToken = new PasswordAuthAuthenticationToken(auth.getAccount(), auth.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(auth.getRole().name())));
        resultToken.setId(auth.getId());
        resultToken.setName(auth.getName());

        return resultToken;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(PasswordAuthAuthenticationToken.class);
    }
}