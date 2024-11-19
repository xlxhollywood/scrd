package org.example.scrd.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;


@Controller
public class HomeController {


    @GetMapping("/home1")
    public String a() {
        return "a";  // a.html을 반환
    }
}


