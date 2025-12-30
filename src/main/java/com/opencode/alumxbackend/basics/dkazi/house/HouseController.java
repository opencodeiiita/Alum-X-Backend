package com.opencode.alumxbackend.basics.dkazi.house;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/house")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping("/hello/{your_name}")
    public String sayHello(@PathVariable String your_name) {
        return houseService.getHelloMessage(your_name);
    }
}