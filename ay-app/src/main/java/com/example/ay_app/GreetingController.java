package com.example.ay_app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    @GetMapping("/")
    public String greeting(
            @RequestParam(defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }
}