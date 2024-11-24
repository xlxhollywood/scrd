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

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // "/error" 및 "/api/ssobbi/auth/"로 시작하는 요청은 JWT 검증 없이 필터를 통과시킴
        if (request.getRequestURI().startsWith("/error") ||
                request.getRequestURI().startsWith("/api/scrd/auth/") ||
                request.getRequestURI().equals("/")
        ) {
            System.out.println("token 필터 - 요청 URI입니다: " + request.getRequestURI());
            filterChain.doFilter(request, response);  // 여기가 올바르게 작동하고 있습니다.
            return;  // 이 부분을 통해 바로 반환

        }


            System.out.println("token 필터 - 요청 URI: " + request.getRequestURI() + " (JWT 검증 시작)");
            // HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            // Header의 Authorization 값이 비어있으면 => Jwt Token을 전송하지 않음 => 로그인 하지 않음
            if (authorizationHeader == null) throw new DoNotLoginException();

            // Header의 Authorization 값이 'Bearer '로 시작하지 않으면 => 잘못된 토큰
            if (!authorizationHeader.startsWith("Bearer "))
                throw new WrongTokenException("Bearer 로 시작하지 않는 토큰입니다.");

            // 전송받은 값에서 'Bearer ' 뒷부분(Jwt Token) 추출
            String token = authorizationHeader.split(" ")[1];
            // 토큰에서 userId를 추출한 뒤 해당 사용자의 정보를 가져옴
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


}
