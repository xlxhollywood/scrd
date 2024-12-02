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

        if (request.getRequestURI().startsWith("/error") ||
                request.getRequestURI().startsWith("/scrd/auth/") ||
                request.getRequestURI().startsWith("/scrd/every") ||
                request.getRequestURI().equals("/")
        ) {
            filterChain.doFilter(request, response);
            return;
        }


            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            // TODO: refresh token 헤더 검증
            String refreshToken = null;

            if (request.getHeader("X-Refresh-Token") != null && !request.getHeader("X-Refresh-Token").isEmpty()) {
                // X-Refresh-Token 헤더에서 리프레시 토큰 값을 바로 가져옴
                refreshToken = request.getHeader("X-Refresh-Token");
            }


            if (authorizationHeader == null) throw new DoNotLoginException();

            if (!authorizationHeader.startsWith("Bearer "))
                throw new WrongTokenException("Bearer 로 시작하지 않는 토큰입니다.");

            String token = authorizationHeader.split(" ")[1];
            System.out.println(refreshToken);
            if (refreshToken == null) {
                User loginUser = authService.getLoginUser(JwtUtil.getUserId(token, SECRET_KEY));
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                loginUser.getId(),
                                null,
                                List.of(new SimpleGrantedAuthority("USER")));
                // 요청의 세부 정보를 설정 (예: IP 주소, 세션 ID 등)
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // SecurityContextHolder에 인증 정보를 설정하여, 이후의 요청이 인증된 상태로 처리되도록 함
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                // 필터 체인의 다음 단계로 요청을 전달
                filterChain.doFilter(request, response);
            }
            // TODO: 헤더에 access token ,refresh token 이 둘 다 있을 경우, access, refresh token을 둘 다 재발급해서 다음 필터로 넘겨준다.
            else {

                System.out.println("refresh toekn 발급을 위한 로직으로 접어들었습니다.");

                // TODO: refresh 토큰 검증 및 access/refresh 토큰 발급
                List<String> newTokens = jwtUtil.validateRefreshToken(token, refreshToken, SECRET_KEY);


                // TODO: 응답 헤더에 access token, refresh token을 심어준다.
                response.setHeader("Authorization", "Bearer " + newTokens.get(0)); // Access Token
                response.setHeader("X-Refresh-Token", newTokens.get(1));


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
