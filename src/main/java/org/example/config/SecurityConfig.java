package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthService authService;

    @Value("${custom.host.client}")  // properties 파일에서 CORS 허용 도메인 리스트를 주입받음
    private List<String> client;

    @Value("${custom.jwt.secret}")  // properties 파일에서 JWT 서명에 사용할 비밀키를 주입받음
    private String SECRET_KEY;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()) // 기본 CORS 설정 적용
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (JWT를 사용하므로 필요 없음)
                .addFilterBefore(new ExceptionHandlerFilter(), UsernamePasswordAuthenticationFilter.class)
                // 모든 요청 전에 ExceptionHandlerFilter를 적용하여 발생하는 예외를 처리
                .addFilterBefore(
                        new JwtTokenFilter(authService, SECRET_KEY), UsernamePasswordAuthenticationFilter.class)
                // JWT 토큰을 인증하기 위한 JwtTokenFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
                .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        request -> request.requestMatchers("/api/ssobbi/auth/**", "/error","/").permitAll())
                .authorizeHttpRequests(
                        request -> request.requestMatchers("/api/ssobbi/**").authenticated());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 Origin 설정 (ex: 클라이언트 도메인)
        config.setAllowedOrigins(client);
        // 허용할 HTTP 메서드 설정
        config.setAllowedMethods(Arrays.asList("POST", "GET", "PATCH", "DELETE", "PUT"));
        // 요청에 허용할 헤더 설정 (Authorization, Content-Type 등)
        config.setAllowedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        // 인증 정보를 포함한 요청(Cookie 등)을 허용할지 여부 설정
        config.setAllowCredentials(true);
        // 특정 경로에 대해 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 설정 적용
        return source; // CORS 설정 반환
    }


}
