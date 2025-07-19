package com.example.DockerNode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.example.DockerNode.InitService.Init;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.DockerNode", "com.example.DockerNode.config", "com.example.DockerNode.ContainerManager"})
public class DockerNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerNodeApplication.class, args);
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {
               Init.StartInitService();
        };
    }
}