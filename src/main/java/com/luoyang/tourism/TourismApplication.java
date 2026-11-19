package com.luoyang.tourism;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableScheduling
@EnableMethodSecurity
@ConfigurationPropertiesScan
@MapperScan("com.luoyang.tourism.module")
@SpringBootApplication
public class TourismApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourismApplication.class, args);
    }
}
