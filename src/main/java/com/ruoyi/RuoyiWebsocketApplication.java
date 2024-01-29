package com.ruoyi;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import com.ruoyi.common.swagger.annotation.EnableCustomSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;


@EnableCustomConfig
@EnableCustomSwagger2
@EnableRyFeignClients
@ServletComponentScan
@SpringBootApplication
public class RuoyiWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuoyiWebsocketApplication.class, args);
    }

}
