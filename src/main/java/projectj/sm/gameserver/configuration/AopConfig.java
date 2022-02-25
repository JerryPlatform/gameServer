package projectj.sm.gameserver.configuration;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import projectj.sm.gameserver.ContextUtil;
import projectj.sm.gameserver.security.JwtAuthTokenProvider;
import projectj.sm.gameserver.vo.Response;
import projectj.sm.gameserver.vo.Result;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
@RequiredArgsConstructor
public class AopConfig {
    @Value("${gameServer.login.retention}")
    private long retentionMinutes;
    private final JwtAuthTokenProvider tokenProvider;

    @AfterReturning(value = "execution(* projectj.sm.gameserver.controller.*.*(..)) && " +
            "!execution(* projectj.sm.gameserver.controller.CommonController.login(..)) " , returning = "result")
    public Object After(Object result) throws Throwable {
        if (result != null){
            Map<String, String> claims = new HashMap<>();
            Date expiredDate = Date.from(LocalDateTime.now().plusMinutes(retentionMinutes).atZone(ZoneId.systemDefault()).toInstant());
            claims.put("id", ContextUtil.getCredential().getId().toString());
            claims.put("account", ContextUtil.getCredential().getAccount());
            claims.put("name", ContextUtil.getCredential().getName());
            claims.put("role", ContextUtil.getCredential().getRole().toString());
            String token = tokenProvider.createAuthToken(ContextUtil.getCredential().getAccount(), ContextUtil.getCredential().getRole().toString(), claims, expiredDate).getToken();
            if(result instanceof Result) {
                ((Result)result).setToken(token);
            } else if (result instanceof Response) {
                ((Response)result).getResponse().setToken(token);
            } else if (result instanceof ResponseEntity){
                Response response = (Response)((ResponseEntity)result).getBody();
                response.getResponse().setToken(token);
            }
        }
        return result;
    }
}
