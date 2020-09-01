package com.fwp.demo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class ProcessApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessApplication.class,args);
    }

}
