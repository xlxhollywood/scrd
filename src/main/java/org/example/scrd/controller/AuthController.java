package org.example.scrd.controller;


import org.example.scrd.util.JwtUtil;
import org.example.scrd.dto.UserDto;
import org.example.scrd.controller.response.KakaoLoginResponse;
import org.example.scrd.service.AuthService;
import org.example.scrd.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // 카카오 로그인을 처리하는 엔드포인트 코드를 받자마자 GetMapping 호출됨
    @GetMapping("/api/scrd/auth/kakao-login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(@RequestParam String code, HttpServletRequest request) {
        // 카카오 로그인 과정: 카카오에서 인증 코드를 받아 사용자의 정보를 가져옴
        // code = token
        UserDto userDto = authService.kakaoLogin(
                kakaoService.kakaoLogin(code));

        // 가져온 사용자 정보로 JWT 토큰을 생성
        String jwtToken = JwtUtil.createToken(userDto.getId(), SECRET_KEY, EXPIRE_TIME_MS);
        System.out.println("Authentication after setting: " + SecurityContextHolder.getContext().getAuthentication());


        return ResponseEntity.ok(
                KakaoLoginResponse.builder() // 리스폰스 객체에다가 JWT 토큰 감싸서 주고 있음.
                        .accessToken(jwtToken)  // 생성한 JWT 토큰
                        .name(userDto.getName())  // 사용자의 이름
                        .profileImageUrl(userDto.getProfileImageUrl()) // 사용자의 프로필 이미지 URL
                        .email(userDto.getEmail()) // 사용자의 전화번호
                        .build());

    }

    @GetMapping("/api/scrd/auth/user")
    public ResponseEntity<String> validateJwtToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("파라미터 값 : " + request);
        System.out.println("Authorization Header: " + authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(7); // "Bearer " 이후의 토큰 추출

        try {
            // 토큰에서 사용자 ID를 추출하고 검증
            Long userId = JwtUtil.getUserId(token, SECRET_KEY);
            authService.getLoginUser(userId); // 사용자 검증 로직

            return ResponseEntity.ok("Token is valid. User ID: " + userId);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
        }
    }

        @GetMapping("/api/scrd/user2")
        public ResponseEntity<String> getCurrentUserId(@AuthenticationPrincipal Long userId) {
            System.out.println("Authentication after setting: " + SecurityContextHolder.getContext().getAuthentication());
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user ID available.");
            }

            return ResponseEntity.ok("Authenticated User ID: " + userId);
        }


}