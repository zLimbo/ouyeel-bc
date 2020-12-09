package com.ouyeel.obfm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ObfmApplication {
    public static void main(String[] args) {
        SpringApplication.run(ObfmApplication.class,args);
    }
}