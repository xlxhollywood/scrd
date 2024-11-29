package org.example.scrd.config;

import lombok.RequiredArgsConstructor;
import org.example.scrd.base.CustomProperties;
import org.example.scrd.filter.ExceptionHandlerFilter;
import org.example.scrd.filter.JwtTokenFilter;
import org.example.scrd.service.AuthService;
import org.example.scrd.util.JwtUtil;
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


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {

    private final AuthService authService;
    private final CustomProperties customProperties;
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()) // 기본 CORS 설정 적용
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (JWT를 사용하므로 필요 없음)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(new ExceptionHandlerFilter(), UsernamePasswordAuthenticationFilter.class)
                // 모든 요청 전에 ExceptionHandlerFilter를 적용하여 발생하는 예외를 처리
                .addFilterBefore(
                        new JwtTokenFilter(authService, customProperties.getJwtSecret(), jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)
                // JWT 토큰을 인증하기 위한 JwtTokenFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
                .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/scrd/auth/**", "/error" ,"/").permitAll()
                        .requestMatchers("/scrd/every/**").permitAll()
                        .requestMatchers("/scrd/api/**").authenticated() // 인증된 사용자만
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 Origin 설정 (ex: 클라이언트 도메인)
        config.setAllowedOrigins(customProperties.getHostClient()); // 클라이언트 도메인
        // 허용할 HTTP 메서드 설정
        config.setAllowedMethods(Arrays.asList("POST", "GET", "PATCH", "DELETE", "PUT"));
        // 요청에 허용할 헤더 설정 (Authorization, Content-Type 등)
        config.setAllowedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        // 노출할 헤더 설정 (클라이언트에서 접근 가능한 헤더)
        config.setExposedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION, "X-Refresh-Token"));
        // 인증 정보를 포함한 요청(Cookie 등)을 허용할지 여부 설정
        config.setAllowCredentials(true);

        // 특정 경로에 대해 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 설정 적용
        return source; // CORS 설정 반환
    }



}
