package org.example.scrd.util;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.scrd.domain.RefreshToken;
import org.example.scrd.exception.WrongTokenException;
import org.example.scrd.repo.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtUtil {

    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${custom.jwt.expire-time-ms}") // JWT 만료 시간을 주입받음
    private long EXPIRE_TIME_MS;
    @Value("${custom.jwt.refresh-expire-time-ms}") // JWT 만료 시간을 주입받음
    private long EXPIRE_REFRESH_TIME_MS;

    public List<String> createToken(Long userId, String secretKey, long expireTimeMs, long expireRefreshTimeMs ) {
        // JWT의 payload에 해당하는 Claims에 데이터를 추가
        // Claim = JWT 토큰의 payload에 저장될 정보. 여기서는 userId를 저장함.
        Claims claims = Jwts.claims();
        claims.put("userId", userId); // 사용자 ID를 Claim에 넣음

        //Access Token 발급
        String accessToken = Jwts.builder()
                .setClaims(claims) // 정보 저장
                .claim("tokenType", "ACCESS") // 토큰 타입 추가
                .setIssuedAt(new Date(System.currentTimeMillis())) // 토큰 발행 시간 정보
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey)  // 사용할 암호화 알고리즘과
                .compact();

        // TODO : Refresh Token 발급
        String refreshToken =  Jwts.builder()
                .setClaims(claims) // 정보 저장
                .claim("tokenType", "REFRESH") // 토큰 타입 추가
                .setIssuedAt(new Date(System.currentTimeMillis())) // 토큰 발행 시간 정보
                .setExpiration(new Date(System.currentTimeMillis() + expireRefreshTimeMs)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey)   // 사용할 암호화 알고리즘과
                .compact();

        // TODO : Refresh Token Redis에 저장해야함
        RefreshToken redis = new RefreshToken(refreshToken, userId);
        refreshTokenRepository.save(redis);

        // 액세스, 리프레쉬가 들어가 있는 토큰 객체를 반환
        return Arrays.asList(accessToken, refreshToken);
    }

    // JWT에서 userId 추출하는 메서드
    public static Long getUserId(String token, String secretKey) {
            // 토큰에서 Claim을 추출하고 userId를 반환
        return extractClaims(token, secretKey).get("userId", Long.class);
    }

    // SecretKey를 사용해 Token을 검증하고, Claim을 추출하는 메서드
    private static Claims extractClaims(String token, String secretKey) {
        try {
            // 토큰을 파싱하여 Claim을 추출
            return Jwts.parser()
                    .setSigningKey(secretKey)  // 서명 검증을 위해 비밀키 설정
                    .parseClaimsJws(token) // 토큰을 파싱하고 유효성 검사를 수행
                    .getBody(); // 유효한 경우 토큰의 본문(Claim)을 반환
        } catch (ExpiredJwtException e) {
            throw new WrongTokenException("만료된 토큰입니다.");

        }
    }

    // TODO: 리프레시 토큰 검증 후, 리프레시/액세스 토큰 발급
    public List<String> validateRefreshToken(String token, String refreshToken, String secretKey) {
        System.out.println("validateRefreshToken 호출됨 ");

        // TODO: 리프레쉬 토큰으로 리프레시 토큰 조회 유효하지 않다면 exception 반환 WrongToken 받으면 프론트는 무조건 로그인 페이지로 보내야한다.
        RefreshToken storedRefreshToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new WrongTokenException("유효하지 않은 리프레시 토큰입니다."));

        // TODO: 유효하다면 리프레쉬 토큰 삭제 후 액세스 토큰 발급
        refreshTokenRepository.deleteById(refreshToken);
        Long userId = storedRefreshToken.getUserId();
        return this.createToken(userId, secretKey, EXPIRE_TIME_MS, EXPIRE_REFRESH_TIME_MS);

    }
}
