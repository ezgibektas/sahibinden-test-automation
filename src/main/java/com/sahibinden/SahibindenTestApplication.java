package com.sahibinden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.sahibinden")
public class SahibindenTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SahibindenTestApplication.class, args);
    }
} 