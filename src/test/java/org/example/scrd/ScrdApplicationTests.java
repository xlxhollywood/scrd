package org.example.scrd;

import org.example.scrd.domain.RefreshToken;
import org.example.scrd.repo.RefreshTokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScrdApplicationTests {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Test
    void findRefreshTokenById() {
        // 1. 테스트용 데이터 저장
        String refreshTokenValue = "testRefreshToken";
        Long userId = 1245L;
        RefreshToken token = new RefreshToken(refreshTokenValue, userId);

        refreshTokenRepository.save(token);

        // 2. 저장된 데이터 조회
        RefreshToken retrievedToken = refreshTokenRepository.findById(refreshTokenValue).orElse(null);

        // 3. 데이터 검증
        Assertions.assertNotNull(retrievedToken, "저장된 토큰은 null이 아니어야 합니다.");
        Assertions.assertEquals(userId, retrievedToken.getUserId(), "User ID가 일치해야 합니다.");
        Assertions.assertEquals(refreshTokenValue, retrievedToken.getRefreshToken(), "Refresh Token 값이 일치해야 합니다.");

        // 4. 결과 출력
        System.out.println("조회된 토큰 값: " + retrievedToken.getRefreshToken());
        System.out.println("조회된 유저 ID: " + retrievedToken.getUserId());
    }
    @Test
    void addDataToRedis() {
        // 새로운 RefreshToken 객체 생성
        String refreshTokenValue = "testRefreshToken";
        Long userId = 12345L;
        RefreshToken token = new RefreshToken(refreshTokenValue, userId);

        // Redis에 저장
        refreshTokenRepository.save(token);

        // 저장한 토큰 조회
        RefreshToken retrievedToken = refreshTokenRepository.findById(refreshTokenValue).orElse(null);

        // 데이터 검증
        Assertions.assertNotNull(retrievedToken, "저장된 토큰은 null이 아니어야 합니다.");
        Assertions.assertEquals(userId, retrievedToken.getUserId(), "User ID가 일치해야 합니다.");

        System.out.println("저장된 토큰 값: " + retrievedToken.getRefreshToken());
        System.out.println("저장된 유저 ID: " + retrievedToken.getUserId());
    }

    @Test
    void deleteDataFromRedis() {

        refreshTokenRepository.deleteById("testRefreshToken");

        // 4. 삭제 후 데이터가 없는지 확인
        RefreshToken deletedToken = refreshTokenRepository.findById("testRefreshToken").orElse(null);
        Assertions.assertNull(deletedToken, "삭제된 토큰은 null이어야 합니다.");

        System.out.println("토큰이 성공적으로 삭제되었습니다: " );
    }
}
