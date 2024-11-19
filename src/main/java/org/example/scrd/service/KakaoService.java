package org.example.scrd.service;

import org.example.scrd.dto.UserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class KakaoService {
    @Value("${kakao.api.key.client}") // application properties에서 카카오 클라이언트 ID 주입
    private String clientId;
    @Value("${kakao.api.redirect-uri}")
    private String redirectUri;



    public UserDto kakaoLogin(String code) {
        String accessToken = getAccessToken(code, redirectUri); // 액세스 토큰을 먼저 받아옴
        return getKakaoUserInfo(accessToken);  // 받은 액세스 토큰으로 사용자 정보 조회
    }
    // 카카오 OAuth 서버에서 액세스 토큰을 받아오는 메서드
    private String getAccessToken(String code, String redirectUri) {

        // HTTP Header 생성 (application/x-www-form-urlencoded 설정)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성 (카카오 API에 전달할 파라미터 설정)
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code"); // 인증 코드 기반의 토큰 요청
        body.add("client_id", clientId); // 카카오 개발자 페이지에서 발급받은 클라이언트 ID
        body.add("redirect_uri", redirectUri); // 리다이렉트 URI
        body.add("code", code); // 카카오 로그인 후 받은 인증 코드

        // HTTP 요청 생성
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        // 카카오 서버로 HTTP 요청을 보내고 액세스 토큰을 받아옴
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",  // 카카오 OAuth 토큰 발급 URL , 여기서 refresh 토큰 발급됨
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        System.out.println("reponse = " + response);

        // HTTP 응답 (JSON)에서 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonNode.get("access_token").asText(); //토큰 전송
    }

    // 액세스 토큰을 사용하여 카카오 사용자 정보를 가져오는 메서드
    private UserDto getKakaoUserInfo(String accessToken) {
        HashMap<String, Object> userInfo= new HashMap<String,Object>();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 생성
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        // 카카오 API 서버로 사용자 정보 요청
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        System.out.println("reponse2" + response.getBody());
        // 응답 본문에서 사용자 정보를 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // 파싱한 사용자 정보를 UserDto 객체로 변환하여 반환
        Long id = jsonNode.get("id").asLong(); // 사용자 고유 ID (카카오 ID)
        String email = jsonNode.get("kakao_account").get("email").asText(); // 이메일
        String nickname = jsonNode.get("properties").get("nickname").asText(); // 사용자 닉네임
        String profileImageUrl = jsonNode.get("properties").get("profile_image").asText(); // 프로필 이미지 URL

        // UserDto 객체를 빌더 패턴을 통해 생성하여 반환
        return UserDto.builder()
                .kakaoId(id)
                .name(nickname)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
