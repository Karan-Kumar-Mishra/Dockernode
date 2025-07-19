package com.example.DockerNode.Schema;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class RequestObjectCreate {
    private String name;
    private String imagename;
}
