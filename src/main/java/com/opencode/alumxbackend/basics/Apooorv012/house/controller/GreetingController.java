package com.opencode.alumxbackend.basics.Apooorv012.house.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.basics.Apooorv012.house.service.HelloService;

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
