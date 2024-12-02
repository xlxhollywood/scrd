package org.example.scrd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefreshTokenSearchService {

    private final RedisTemplate<String, String> redisTemplate;

    public void validateRefreshToken(String token) {
        // 1. Redis에서 모든 Key 가져오기
        Set<String> keys = redisTemplate.keys("refreshToken:*");

        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("No refresh tokens found");
        }


        // 2. 모든 Key의 Hash Value 검사
        for (String key : keys) {
            // "refreshToken" 필드 값 가져오기
            String refreshTokenValue = (String) redisTemplate.opsForHash().get(key, "refreshToken");
            if (refreshTokenValue != null && refreshTokenValue.equals(token)) {
                // 요청된 Token과 Hash Value의 "refreshToken" 필드가 일치하면 성공
                return;
            }
        }

        // 3. Token이 없으면 예외 발생
        throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
    }
}


