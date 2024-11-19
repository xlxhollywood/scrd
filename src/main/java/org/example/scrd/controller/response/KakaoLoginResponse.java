package org.example.scrd.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KakaoLoginResponse {
    private String accessToken;
    private String name;
    private String profileImageUrl;
    private String phoneNumber;
}