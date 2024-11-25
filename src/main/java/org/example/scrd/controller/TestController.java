package org.example.scrd.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;


@Controller
public class TestController {

    @GetMapping("/scrd/api/test")
    public String TestApi() {
        System.out.println("test api 요청 호출");
        return "test api success";
    }

    @GetMapping("/scrd/every")
    public String EveryApi() {
        System.out.println("every api 요청 호출");
        return "every api success";
    }
}


