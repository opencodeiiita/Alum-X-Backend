package com.opencode.alumxbackend.basics.sanskarvns.house.controller;

import com.opencode.alumxbackend.basics.Apooorv012.house.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greet")
public class GreetingController {

    private final HelloService greetingService;

    public GreetingController(HelloService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping
    public String greetUser(String name) {
        return greetingService.getHelloMessage();
    }
}
