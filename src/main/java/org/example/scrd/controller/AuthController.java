package org.example.scrd.controller;


import org.example.scrd.util.JwtUtil;
import org.example.scrd.dto.UserDto;
import org.example.scrd.controller.response.KakaoLoginResponse;
import org.example.scrd.service.AuthService;
import org.example.scrd.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // 사용자 인증 관련 서비스

    private final KakaoService kakaoService; // 카카오 API와 통신하는 서비스

    @Value("${custom.jwt.secret}") // application properties에서 JWT 비밀키를 주입받음
    private String SECRET_KEY;

    @Value("${custom.jwt.expire-time-ms}") // JWT 만료 시간을 주입받음
    private long EXPIRE_TIME_MS;

    // 카카오 로그인을 처리하는 엔드포인트 코드를 받자마자 GetMapping 호출됨
    @GetMapping("/api/scrd/auth/kakao-login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(@RequestParam String code, HttpServletRequest request) {
        System.out.println(code);
        System.out.println("request" + request);
        // 카카오 로그인 과정: 카카오에서 인증 코드를 받아 사용자의 정보를 가져옴
        // code = token
        UserDto userDto = authService.kakaoLogin(
                kakaoService.kakaoLogin(code));

        // 가져온 사용자 정보로 JWT 토큰을 생성
        String jwtToken = JwtUtil.createToken(userDto.getId(), SECRET_KEY, EXPIRE_TIME_MS);

        return ResponseEntity.ok(
                KakaoLoginResponse.builder()
                        .accessToken(jwtToken)  // 생성한 JWT 토큰
                        .name(userDto.getName())  // 사용자의 이름
                        .profileImageUrl(userDto.getProfileImageUrl()) // 사용자의 프로필 이미지 URL
                        .email(userDto.getEmail()) // 사용자의 전화번호
                        .build());
    }
}