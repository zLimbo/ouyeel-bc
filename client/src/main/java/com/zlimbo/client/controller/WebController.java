package com.zlimbo.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("")
public class WebController {

    @RequestMapping("/index")
    public ModelAndView index() {
        System.out.println("====index");
        ModelAndView modelAndView = new ModelAndView("index");
        return modelAndView;
    }


    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }
}
