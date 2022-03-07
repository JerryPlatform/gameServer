package projectj.sm.gameserver.service.Impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectj.sm.gameserver.ContextUtil;
import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.dto.MemberDto;
import projectj.sm.gameserver.dto.MemberUseDto;
import projectj.sm.gameserver.exception.ErrorCode;
import projectj.sm.gameserver.repository.MemberRepository;
import projectj.sm.gameserver.security.JwtAuthTokenProvider;
import projectj.sm.gameserver.security.PasswordAuthAuthenticationToken;
import projectj.sm.gameserver.service.MemberService;
import projectj.sm.gameserver.vo.MemberVo;
import projectj.sm.gameserver.vo.Result;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Log
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    @Value("${gameServer.login.retention}")
    private long retentionMinutes;
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthTokenProvider tokenProvider;
    private Integer temporaryUserId = 5000;

    private List<String> prefix = new ArrayList<>(Arrays.asList("똑똑한", "귀여운", "무식한", "착한", "나쁜", "못생긴", "잘생긴", "신기한"));
    private List<String> noun = new ArrayList<>(Arrays.asList("쥐", "소", "호랑이", "토끼", "용", "뱀", "말", "양", "원숭이", "닭", "개", "돼지"));

    @Override
    public PasswordAuthAuthenticationToken passwordAuth(String account, String password) throws Exception {
        PasswordAuthAuthenticationToken token = new PasswordAuthAuthenticationToken(account, password);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        } catch (LockedException e) { throw new LockedException(ErrorCode.LOCKED.getMessage()); }
          catch (Exception e) { throw new Exception(ErrorCode.LOGIN_FAILED.getMessage()); }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (PasswordAuthAuthenticationToken) authentication;
    }

    @Override
    public List<Member> getMemberList() {
        return memberRepository.findAll();
    }

    @Override
    public String getKakaoAccessToken(String code) {
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=10f8d2706bdb83ab47cae05209b690ea");  //본인이 발급받은 key
            sb.append("&redirect_uri=https://5e06-221-164-197-252.ngrok.io/v1/kakao/login");     // 본인이 설정해 놓은 경로
            sb.append("&code=" + code);
            bw.write(sb.toString());
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            access_Token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

            br.close();
            bw.close();
        } catch (IOException e) { }
        return access_Token;
    }

    @Transactional
    @Override
    public void memberSave(MemberDto dto) {
        Member member;
        if(dto.getId() == null) {
            member = new Member();
            member.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            member = memberRepository.findById(dto.getId()).get();
            if(dto.getPassword() != null) {
                member.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        }
        member.setAccount(dto.getAccount());
        member.setName(dto.getName());
        member.setValid(true);
        member.setRole(Member.Role.ROLE_USER);
        memberRepository.save(member);
    }

    @Override
    public MemberVo temporaryMemberIssuance(MemberUseDto dto) {
        HashMap<String, Object> userInfo = getKakaoUserInfo(dto.getAccessToken());
        if (userInfo.get("nickname") != null) {
            Date expiredDate = Date.from(LocalDateTime.now().plusMinutes(retentionMinutes).atZone(ZoneId.systemDefault()).toInstant());

            Integer temporaryId = ++temporaryUserId;
            String temporaryNickName =
                    prefix.get(ThreadLocalRandom.current().nextInt(0, prefix.size()-1)) +
                    noun.get(ThreadLocalRandom.current().nextInt(0, noun.size()-1));

            Map<String, String> claims = new HashMap<>();
            claims.put("id", temporaryId.toString());
            claims.put("account", temporaryNickName);
            claims.put("name", temporaryNickName);
            claims.put("role", Member.Role.ROLE_USER.toString());
            String token = tokenProvider.createAuthToken(temporaryNickName, Member.Role.ROLE_USER.toString(), claims, expiredDate).getToken();

            return MemberVo.builder()
                    .id(temporaryId.longValue())
                    .account(temporaryNickName)
                    .name(temporaryNickName)
                    .token(token)
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public Result getNewToken() {
        Map<String, String> claims = new HashMap<>();
        Date expiredDate = Date.from(LocalDateTime.now().plusMinutes(retentionMinutes).atZone(ZoneId.systemDefault()).toInstant());
        claims.put("id", ContextUtil.getCredential().getId().toString());
        claims.put("account", ContextUtil.getCredential().getAccount());
        claims.put("name", ContextUtil.getCredential().getName());
        claims.put("role", ContextUtil.getCredential().getRole().toString());
        String token = tokenProvider.createAuthToken(ContextUtil.getCredential().getAccount(), ContextUtil.getCredential().getRole().toString(), claims, expiredDate).getToken();

        return Result.builder()
                .message("success")
                .status(200)
                .errorCode(null)
                .token(token)
                .build();
    }

    public HashMap<String, Object> getKakaoUserInfo(String accessToken) {
        HashMap<String, Object> userInfo = new HashMap<>();
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

            String nickname = properties.getAsJsonObject().get("nickname").getAsString();

            userInfo.put("nickname", nickname);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userInfo;
    }
}
