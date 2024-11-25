package org.example.scrd.controller;


import org.example.scrd.domain.Token;
import org.example.scrd.util.JwtUtil;
import org.example.scrd.dto.UserDto;
import org.example.scrd.controller.response.KakaoLoginResponse;
import org.example.scrd.service.AuthService;
import org.example.scrd.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // 사용자 인증 관련 서비스

    private final KakaoService kakaoService; // 카카오 API와 통신하는 서비스

    @Value("${custom.jwt.secret}") // application properties에서 JWT 비밀키를 주입받음
    private String SECRET_KEY;

    @Value("${custom.jwt.expire-time-ms}") // JWT 만료 시간을 주입받음
    private long EXPIRE_TIME_MS;
    @Value("${custom.jwt.refresh-expire-time-ms}") // JWT 만료 시간을 주입받음
    private long EXPIRE_REFRESH_TIME_MS;

    // 카카오 로그인을 처리하는 엔드포인트 코드를 받자마자 GetMapping 호출됨
    // http://localhost:8080/scrd/auth/kakao-login"
    @GetMapping("/scrd/auth/kakao-login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(@RequestParam String code, HttpServletRequest request) {
        // 카카오 로그인 과정: 카카오에서 인증 코드를 받아 사용자의 정보를 가져옴
        // code = token
        System.out.println("호출됨");
        System.out.println("code : " + code);
        System.out.println("Header : " + request.getHeader("Origin")+"/login/oauth/kakao");
        UserDto userDto =
                authService.kakaoLogin(
                        kakaoService.kakaoLogin(code,request.getHeader("Origin")+"/login/oauth/kakao"));

        // 가져온 사용자 정보로 JWT 토큰을 생성
        Token jwtToken = JwtUtil.createToken(userDto.getId(), SECRET_KEY, EXPIRE_TIME_MS, EXPIRE_REFRESH_TIME_MS);
        System.out.println("Authentication after setting: " + SecurityContextHolder.getContext().getAuthentication());


        return ResponseEntity.ok(
                KakaoLoginResponse.builder() // 리스폰스 객체에다가 JWT 토큰 감싸서 주고 있음.
                        .token(jwtToken)  // 생성한 JWT 토큰
                        .name(userDto.getName())  // 사용자의 이름
                        .profileImageUrl(userDto.getProfileImageUrl()) // 사용자의 프로필 이미지 URL
                        .email(userDto.getEmail()) // 사용자의 전화번호
                        .build());

    }

}