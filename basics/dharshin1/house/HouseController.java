package com.opencode.alumxbackend.basics.dharshin1.house;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping("/house/hello")
    public String helloHouse() {
        return houseService.getHelloMessage();
    }
}
