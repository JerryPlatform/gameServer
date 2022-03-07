package projectj.sm.gameserver.service.Impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.dto.MemberDto;
import projectj.sm.gameserver.dto.MemberUseDto;
import projectj.sm.gameserver.exception.ErrorCode;
import projectj.sm.gameserver.repository.MemberRepository;
import projectj.sm.gameserver.security.PasswordAuthAuthenticationToken;
import projectj.sm.gameserver.service.MemberService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

@Log
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PasswordAuthAuthenticationToken passwordAuth(String account, String password) {
        PasswordAuthAuthenticationToken token = new PasswordAuthAuthenticationToken(account, password);
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(token);
        } catch (LockedException e) { throw new LockedException(ErrorCode.LOCKED.getMessage()); }
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
        member.setValid(false);
        member.setRole(Member.Role.ROLE_USER);
        memberRepository.save(member);
    }

    @Transactional
    @Override
    public boolean memberUse(MemberUseDto dto) {
        HashMap<String, Object> userInfo = getKakaoUserInfo(dto.getAccessToken());
        if (userInfo.get("nickname") != null) {
            Member member = memberRepository.getById(dto.getMemberId());
            member.setValid(true);
            memberRepository.save(member);
            return true;
        } else {
            return false;
        }
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
