package org.example.scrd.controller.response;

import lombok.Builder;
import lombok.Getter;
import org.example.scrd.domain.Token;

@Builder
@Getter
public class KakaoLoginResponse {
    private Token token;
    private String name;
    private String profileImageUrl;
    private String email;
}