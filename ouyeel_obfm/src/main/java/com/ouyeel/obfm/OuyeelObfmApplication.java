package com.ouyeel.obfm;

import com.ouyeel.obfm.fm.business.impl.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OuyeelObfmApplication {
    final static Logger logger = LoggerFactory.getLogger(OuyeelObfmApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OuyeelObfmApplication.class, args);
    }

}
