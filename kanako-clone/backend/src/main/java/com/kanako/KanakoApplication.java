package com.kanako;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kanako.modules.**.mapper")
public class KanakoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KanakoApplication.class, args);
    }
}