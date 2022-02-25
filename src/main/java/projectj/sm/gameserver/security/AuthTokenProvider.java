package projectj.sm.gameserver.security;

import org.springframework.security.core.Authentication;
import java.util.Date;
import java.util.Map;
public interface AuthTokenProvider<T> {
    T createAuthToken(String id, String role, Map<String, String> claims, Date expiredDate);
    T convertAuthToken(String token);
    Authentication getAuthentication(T authToken);
}
