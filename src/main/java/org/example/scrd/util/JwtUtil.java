package org.example.scrd.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.scrd.exception.WrongTokenException;

import java.util.Date;
public class JwtUtil {
    public static String createToken(Long userId, String secretKey, long expireTimeMs) {
        // JWT의 payload에 해당하는 Claims에 데이터를 추가
        // Claim = JWT 토큰의 payload에 저장될 정보. 여기서는 userId를 저장함.
        Claims claims = Jwts.claims();
        claims.put("userId", userId); // 사용자 ID를 Claim에 넣음

        // JWT 토큰을 생성
        return Jwts.builder()
                .setClaims(claims)// 사용자 ID가 포함된 Claim 설정
                .setIssuedAt(new Date(System.currentTimeMillis())) // 토큰이 발급된 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs)) // 토큰 만료 시간 설정
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 알고리즘과 비밀키를 사용해 서명
                .compact(); // JWT 토큰을 직렬화하여 반환 = 객체나 데이터를 특정 포맷(예: 문자열)으로 변환하는 것을 의미합니다.
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
            // 만료된 토큰의 경우 예외를 발생시킴
            throw new WrongTokenException("만료된 토큰입니다.");
        }
    }
}
