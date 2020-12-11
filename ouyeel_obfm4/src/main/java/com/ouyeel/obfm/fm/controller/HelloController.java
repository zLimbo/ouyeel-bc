package com.ouyeel.obfm.fm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public void sayHello(){
        System.out.println("hello");
    }
}
