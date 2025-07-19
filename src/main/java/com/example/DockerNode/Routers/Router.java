package com.example.DockerNode.Routers;

import com.example.DockerNode.ContainerManager.DockerService;
import com.example.DockerNode.Schema.RequestObjectCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/")
public class Router {

    private final DockerService dockerService;

    @Autowired
    public Router(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @GetMapping("/check")
    public String check() {
        return "ok";
    }

    @PostMapping("/create")
    public ResponseEntity<?> createContainer(@RequestBody RequestObjectCreate res) {
        try {
            // Validate request body
            if (res == null || res.getName() == null || res.getImagename() == null ||
                    res.getName().isBlank() || res.getImagename().isBlank()) {
                return new ResponseEntity<>("Container name and image name must not be null or empty",
                        HttpStatus.BAD_REQUEST);
            }
       
            String id = dockerService.createAndStartContainer(res.getImagename(), res.getName());
            System.out.println("Request: " + res);
            return new ResponseEntity<>("Container '" + id + "' is running successfully.", HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating container: " + e.getMessage());
            return new ResponseEntity<>("Failed to create container: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startContainer(@RequestBody ContainerRequest request) {
        try {
            if (request == null || request.getContainerId() == null || request.getContainerId().isBlank()) {
                return new ResponseEntity<>("Container ID must not be null or empty", HttpStatus.BAD_REQUEST);
            }
            dockerService.startContainer(request.getContainerId());
            return new ResponseEntity<>("Container '" + request.getContainerId() + "' started successfully.",
                    HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error starting container: " + e.getMessage());
            return new ResponseEntity<>("Failed to start container: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopContainer(@RequestBody ContainerRequest request) {
        try {
            if (request == null || request.getContainerId() == null || request.getContainerId().isBlank()) {
                return new ResponseEntity<>("Container ID must not be null or empty", HttpStatus.BAD_REQUEST);
            }
            dockerService.stopContainer(request.getContainerId());
            return new ResponseEntity<>("Container '" + request.getContainerId() + "' stopped successfully.",
                    HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error stopping container: " + e.getMessage());
            return new ResponseEntity<>("Failed to stop container: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteContainer(@RequestBody ContainerRequest request) {
        try {
            if (request == null || request.getContainerId() == null || request.getContainerId().isBlank()) {
                return new ResponseEntity<>("Container ID must not be null or empty", HttpStatus.BAD_REQUEST);
            }
            dockerService.removeContainer(request.getContainerId());
            return new ResponseEntity<>("Container '" + request.getContainerId() + "' deleted successfully.",
                    HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error deleting container: " + e.getMessage());
            return new ResponseEntity<>("Failed to delete container: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

// DTO for start, stop, and delete endpoints
class ContainerRequest {
    private String containerId;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}