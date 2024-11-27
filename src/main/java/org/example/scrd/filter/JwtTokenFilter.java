package org.example.scrd.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.scrd.domain.User;
import org.example.scrd.exception.DoNotLoginException;
import org.example.scrd.exception.WrongTokenException;
import org.example.scrd.service.AuthService;
import org.example.scrd.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtTokenFilter extends  OncePerRequestFilter{
    private final AuthService authService; // 사용자 정보를 가져오는 서비스
    private final String SECRET_KEY; // JWT 서명 검증에 사용할 비밀 키
    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // "/error" 및 "/api/ssobbi/auth/"로 시작하는 요청은 JWT 검증 없이 필터를 통과시킴
        if (request.getRequestURI().startsWith("/error") ||
                request.getRequestURI().startsWith("/scrd/auth/") ||
                request.getRequestURI().startsWith("/scrd/every") ||
                request.getRequestURI().equals("/")
        ) {
            filterChain.doFilter(request, response);  // 여기가 올바르게 작동하고 있습니다.
            return;  // 이 부분을 통해 바로 반환
        }

            // HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            // TODO: refresh token 헤더 검증
            String refreshToken = null;
            if (request.getHeader("X-Refresh-Token") != null && !request.getHeader("X-Refresh-Token").isEmpty()) {
            String refreshTokenHeader = request.getHeader("X-Refresh-Token");
            if (refreshTokenHeader.startsWith("Bearer ")) {
                refreshToken = refreshTokenHeader.substring(7); // Bearer 이후의 값 추출
                }
            }

            // Header의 Authorization 값이 비어있으면 => Jwt Token을 전송하지 않음 => 로그인 하지 않음
            if (authorizationHeader == null) throw new DoNotLoginException();

            // Header의 Authorization 값이 'Bearer '로 시작하지 않으면 => 잘못된 토큰
            if (!authorizationHeader.startsWith("Bearer "))
                throw new WrongTokenException("Bearer 로 시작하지 않는 토큰입니다.");
            // 전송받은 값에서 'Bearer ' 뒷부분(Jwt Token) 추출
            String token = authorizationHeader.split(" ")[1];

            // 헤어데 refresh token이 null일 경우 기존 로직대로 처리
            if (refreshToken == null) {
                User loginUser = authService.getLoginUser(JwtUtil.getUserId(token, SECRET_KEY));
                // loginUser 정보로 UsernamePasswordAuthenticationToken 발급
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                loginUser.getId(), // 사용자 ID (식별자)
                                null,  // 비밀번호는 null로 설정 (JWT로 이미 인증됨)
                                List.of(new SimpleGrantedAuthority("USER"))); // 사용자의 권한 (여기서는 "USER")
                // 요청의 세부 정보를 설정 (예: IP 주소, 세션 ID 등)
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContextHolder에 인증 정보를 설정하여, 이후의 요청이 인증된 상태로 처리되도록 함
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                // 필터 체인의 다음 단계로 요청을 전달
                filterChain.doFilter(request, response);
            }
            // TODO: 헤더에 access token ,refresh token 이 둘 다 있을 경우, access, refresh token을 둘 다 재발급해서 다음 필터로 넘겨준다.
            else {
                // TODO: refresh 토큰 검증 및 access/refresh 토큰 발급
                List<String> newTokens = jwtUtil.validateRefreshToken(token, refreshToken, SECRET_KEY);

                // TODO: 응답 헤더에 access token, refresh token을 심어준다.
                response.setHeader("Authorization", "Bearer " + newTokens.get(0)); // Access Token
                response.setHeader("X-Refresh-Token", newTokens.get(1));          // Refresh Token

                // TODO: User 객체에 새로운 액세스 토큰으로 ID 찾아오기
                User loginUser = authService.getLoginUser(JwtUtil.getUserId(newTokens.get(0),SECRET_KEY));

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                loginUser.getId(), // 사용자 ID (식별자)
                                null,  // 비밀번호는 null로 설정 (JWT로 이미 인증됨)
                                List.of(new SimpleGrantedAuthority("USER"))); // 사용자의 권한 (여기서는 "USER")
                // 요청의 세부 정보를 설정 (예: IP 주소, 세션 ID 등)
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContextHolder에 인증 정보를 설정하여, 이후의 요청이 인증된 상태로 처리되도록 함
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                // 필터 체인의 다음 단계로 요청을 전달
                filterChain.doFilter(request, response);

            }
    }


}
