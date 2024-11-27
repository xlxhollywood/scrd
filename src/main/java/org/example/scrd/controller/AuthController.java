package org.example.scrd.controller;


import jakarta.servlet.http.HttpServletResponse;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // 사용자 인증 관련 서비스

    private final KakaoService kakaoService; // 카카오 API와 통신하는 서비스

    private final JwtUtil jwtUtil;

    @Value("${custom.jwt.secret}") // application properties에서 JWT 비밀키를 주입받음
    private String SECRET_KEY;

    @Value("${custom.jwt.expire-time-ms}") // JWT 만료 시간을 주입받음
    private long EXPIRE_TIME_MS;
    @Value("${custom.jwt.refresh-expire-time-ms}") // JWT 만료 시간을 주입받음
    private long EXPIRE_REFRESH_TIME_MS;

    // 카카오 로그인을 처리하는 엔드포인트 코드를 받자마자 GetMapping 호출됨
    // http://localhost:8080/scrd/auth/kakao-login"
    @GetMapping("/scrd/auth/kakao-login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(
            @RequestParam String code,
            HttpServletRequest request,
            HttpServletResponse response) { // HttpServletResponse 추가

        // 카카오 로그인 과정
        System.out.println("호출됨");
        System.out.println("code : " + code);
        System.out.println("Header : " + request.getHeader("Origin") + "/login/oauth/kakao");
        UserDto userDto =
                authService.kakaoLogin(
                        kakaoService.kakaoLogin(code, request.getHeader("Origin") + "/login/oauth/kakao"));

        // JWT 토큰 생성
        List<String> jwtToken = jwtUtil.createToken(userDto.getId(), SECRET_KEY, EXPIRE_TIME_MS, EXPIRE_REFRESH_TIME_MS);
        System.out.println("Authentication after setting: " + SecurityContextHolder.getContext().getAuthentication());

        // TODO: 액세스 토큰을 Authorization 헤더에 추가
        response.setHeader("Authorization", "Bearer " + jwtToken.get(0));

        // TODO: 액세스 토큰을 Authorization 헤더에 추가
        response.setHeader("X-Refresh-Token", jwtToken.get(1));

        // 응답 본문에 JWT 토큰 및 사용자 정보 추가
        return ResponseEntity.ok(
                KakaoLoginResponse.builder()
                        .name(userDto.getName())
                        .profileImageUrl(userDto.getProfileImageUrl())
                        .email(userDto.getEmail())
                        .build());
    }
}
