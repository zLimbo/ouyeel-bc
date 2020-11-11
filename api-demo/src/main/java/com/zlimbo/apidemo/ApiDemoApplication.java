package com.zlimbo.apidemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zlimbo.apidemo.mapper")
public class ApiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiDemoApplication.class, args);
    }

}
