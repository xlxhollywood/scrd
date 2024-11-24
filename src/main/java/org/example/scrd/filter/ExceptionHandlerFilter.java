package org.example.scrd.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import org.example.scrd.controller.response.ExceptionResponse;
import org.example.scrd.exception.DoNotLoginException;
import org.example.scrd.exception.WrongTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class ExceptionHandlerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 요청을 처리하는 필터 체인을 실행 try/ catch 문이므로 jwt 토큰에서 예외가 발생하면 이 필터에서 예외처리를 해줄 것이다.
            filterChain.doFilter(request, response);
        } catch (DoNotLoginException e) {
            // 로그인하지 않았을 경우 발생하는 예외 처리 (ex: 토큰이 없는 경우)
            // HTTP 응답 상태를 401 UNAUTHORIZED로 설정
            setErrorResponse(response, e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (WrongTokenException e) {
            // 잘못된 토큰을 사용할 경우 발생하는 예외 처리
            // HTTP 응답 상태를 401 UNAUTHORIZED로 설정
            setErrorResponse(response, e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private void setErrorResponse(
            HttpServletResponse response, String message, HttpStatus httpStatus) {
        ObjectMapper objectMapper = new ObjectMapper(); // JSON으로 변환하기 위한 객체
        response.setStatus(httpStatus.value());  // HTTP 응답 상태 코드 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 응답 콘텐츠 타입을 JSON으로 설정
        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // 응답 인코딩을 UTF-8로 설정

        // ExceptionResponse 객체 생성 (에러 메시지와 상태 정보를 포함)
        ExceptionResponse exceptionResponse =
                ExceptionResponse.builder().error(httpStatus.getReasonPhrase()).message(message).build();
        try {
            // 생성한 에러 정보를 JSON으로 변환하여 응답에 작성
            response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
        } catch (IOException e) {
            e.printStackTrace(); // 에러 발생 시 스택 트레이스를 출력
        }
    }
}
