package org.example.scrd.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash(value = "refreshToken", timeToLive = 14440) // 4시간
@AllArgsConstructor
public class RefreshToken {

    @Id
    private Long userId;
    private String refreshToken;

}
