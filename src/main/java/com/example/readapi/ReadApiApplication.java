package com.example.readapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReadApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReadApiApplication.class, args);
    }
}
