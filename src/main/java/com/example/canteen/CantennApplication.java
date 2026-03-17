package com.example.canteen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.canteen.mapper")
public class CantennApplication {
    public static void main(String[] args) {
        SpringApplication.run(CantennApplication.class, args);
    }

}
