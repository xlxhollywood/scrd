package org.example.scrd.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {



    @GetMapping("/scrd/api/test")
    public String TestApi(HttpServletRequest request) {
        System.out.println("==========받은 헤더 , 즉 기존에 있던 액세스, 리프레쉬 토큰");

        // 출력할 헤더 이름 목록
        String[] targetHeaders = {HttpHeaders.AUTHORIZATION, "X-Refresh-Token"};

        // 헤더 출력
        for (String headerName : targetHeaders) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null) {
                System.out.println(headerName + ": " + headerValue);
            } else {
                System.out.println(headerName + ": [헤더 값 없음]");
            }
        }

        return "test api success - check server logs for specific headers";
    }


    @GetMapping("/scrd/every")
    public String EveryApi() {
        System.out.println("every api 요청 호출");
        return "every api success";
    }
}



