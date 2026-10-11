package cn.luoyang.tourism;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.luoyang.tourism.infrastructure.mapper")
public class LuoyangApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuoyangApplication.class, args);
    }
}
