package projectj.sm.gameserver.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

@Getter @Setter
public class PasswordAuthAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private Long id;
    private String account;
    private String name;

    private String position;
    private Long deptId;
    private String deptNm;
    private Long corpId;
    private String corpNm;
    private String corpDomain;
    private String authority;
    private Long authorityId;
    private Long division;

    public PasswordAuthAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public PasswordAuthAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
