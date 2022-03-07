package projectj.sm.gameserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;
import projectj.sm.gameserver.domain.Member;
import projectj.sm.gameserver.dto.LoginDto;
import projectj.sm.gameserver.dto.MemberDto;
import projectj.sm.gameserver.dto.MemberUseDto;
import projectj.sm.gameserver.security.JwtAuthToken;
import projectj.sm.gameserver.security.JwtAuthTokenProvider;
import projectj.sm.gameserver.security.PasswordAuthAuthenticationToken;
import projectj.sm.gameserver.service.MemberService;
import projectj.sm.gameserver.vo.MemberVo;
import projectj.sm.gameserver.vo.Response;
import projectj.sm.gameserver.vo.Result;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Log
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class CommonController {
    @Value("${gameServer.login.retention}")
    private long retentionMinutes;
    @Autowired
    private JwtAuthTokenProvider tokenProvider;

    private final MemberService memberService;

    private Function<PasswordAuthAuthenticationToken, MemberVo> makeToken = user -> {
        try {
            Date expiredDate = Date.from(LocalDateTime.now().plusMinutes(retentionMinutes).atZone(ZoneId.systemDefault()).toInstant());
            Map<String, String> claims = new HashMap<>();
            claims.put("id", user.getId().toString());
            claims.put("account", user.getPrincipal().toString());
            claims.put("name", user.getName());

            JwtAuthToken token = tokenProvider.createAuthToken(user.getPrincipal().toString(), user.getAuthorities().iterator().next().getAuthority(), claims, expiredDate);
            MemberVo data = MemberVo.builder()
                    .id(user.getId())
                    .account(user.getPrincipal().toString())
                    .name(user.getName())
                    .token(token.getToken())
                    .build();
            return data;
        } catch (BadCredentialsException e) {
            return null;
        }
    };

    @PutMapping("/member/save")
    public void memverSave(@Valid @RequestBody MemberDto memberDto) {
        memberService.memberSave(memberDto);
    }

    @GetMapping("/kakao/login")
    public String kakaoLogin(@RequestParam(value = "code", required = false) String code) {
        String accessToken = memberService.getKakaoAccessToken(code);
        return accessToken;
    }

    @PutMapping("/member/use")
    public ResponseEntity<Response> memberUse(@RequestBody MemberUseDto dto) {
        if (memberService.memberUse(dto)) {
            return new ResponseEntity<>(Response.builder().response(Result.builder().build()).contents("success").build(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Response.builder().response(Result.builder().message("accessToken error").status(500).build()).contents("accessToken error").build(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody LoginDto loginDto) {
        Result response = Result.builder().build();
        PasswordAuthAuthenticationToken user;
        try {
            user = memberService.passwordAuth(loginDto.getAccount(), loginDto.getPassword());
        } catch (LockedException e) {
            return new ResponseEntity<>(Response.builder()
                    .response(Result.builder().message("kakao login authentication required").status(401).build())
                    .contents("kakao login authentication required")
                    .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        MemberVo vo = makeToken.apply(user);
        if (vo == null) {
            return new ResponseEntity<>(Response.<MemberVo>builder().response(Result.builder().status(500).message("error")
                    .build()).contents(null).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>(Response.<MemberVo>builder().response(response).contents(vo).build(), HttpStatus.OK);
        }
    }
}
