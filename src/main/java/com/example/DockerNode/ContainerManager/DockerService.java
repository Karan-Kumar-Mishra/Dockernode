package com.example.DockerNode.ContainerManager;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;


import org.springframework.stereotype.Service;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.ListNetworksCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DockerService {

    public final DockerClient dockerClient;
    private static final Logger logger = LoggerFactory.getLogger(DockerService.class);
    
 

    public DockerService() {
        try {
            // Configure Docker client
            DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("tcp://0.0.0.0:2375")
                    .withDockerTlsVerify(false)
                    .build();
                        logger.info("Configured Docker Host: {}", config.getDockerHost());

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .build();

            this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
            pingDockerDaemon();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize DockerClient: " + e.getMessage(), e);
            // System.out.println("Failed to create/start container: " + e.getMessage());
        }
    }

    private void pingDockerDaemon() {
        try {
            dockerClient.pingCmd().exec();
            logger.info("Successfully connected to Docker daemon ");
        } catch (Exception e) {
            logger.error("Failed to connect to Docker daemon: {}", e.getMessage(), e);
        }
    }

 

    private boolean isWsl() {
        // Check if running in WSL
        String osRelease = System.getenv("WSL_DISTRO_NAME");
        return osRelease != null && !osRelease.isEmpty();
    }

    // Create and start a container
    public String createAndStartContainer(String imageName, String containerName) {
        try {
            System.out.println("try to create container....");
            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                    .withName(containerName)
                    .exec();
            return startContainer(container.getId());
        } catch (Exception e) {
            System.out.println("Failed to create/start container: " + e.getMessage());
            return null;
        }
    }

    // Start a container
    public String startContainer(String containerId) {
        try {
            System.out.println("try to start container....");
            dockerClient.startContainerCmd(containerId).exec();
            return containerId;
        } catch (Exception e) {
            System.out.println("Failed to start container: " + e.getMessage());
            return null;
        }
    }

    // Stop a container
    public void stopContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
        } catch (Exception e) {
            System.out.println("Failed to stop container: " + e.getMessage());
        }
    }

    // Remove a container
    public void removeContainer(String containerId) {
        try {
            dockerClient.removeContainerCmd(containerId).exec();
        } catch (Exception e) {
            System.out.println("Failed to remove container: " + e.getMessage());
        }
    }

    // pull image
    public void pullImage(String imageName) {
        PullImageResultCallback callback = new PullImageResultCallback() {
            @Override
            public void onNext(com.github.dockerjava.api.model.PullResponseItem item) {
                logger.info("Pull progress: {} - {}", item.getId(), item.getStatus());
                super.onNext(item);
            }

            @Override
            public void onComplete() {
                logger.info("Successfully pulled image: {}", imageName);
                super.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Failed to pull image {}: {}", imageName, throwable.getMessage(), throwable);
                super.onError(throwable);
            }
        };
        try {
            dockerClient.pullImageCmd(imageName).exec(callback).awaitCompletion();
        } catch (Exception e) {
            System.out.println("Failed to pull image " + e.getMessage());
        }
    }

    public void createNetwork(String networkName) {
        try {
            logger.info("Creating Docker network: DockerNodeNetwork");
            CreateNetworkResponse response = dockerClient.createNetworkCmd()
                    .withName(networkName)
                    .withDriver("bridge")
                    .exec();
            logger.info("Successfully created network with ID: {}", response.getId());
        } catch (Exception e) {
            System.out.println("Faild to delete the docker container " + e.getMessage());
        }
    }

    public void deleteNetwork(String networkName) {
        try {
            logger.info("deleteing Docker network: DockerNodeNetwork");
            dockerClient.removeNetworkCmd(getNetworkID(networkName));
            logger.info("Successfully delete network with ID: {}");
        } catch (Exception e) {
            System.out.println("Faild to delete the network " + e.getMessage());
        }
    }

    String getNetworkID(String newtworkName) {
        try {
            ListNetworksCmd list = dockerClient.listNetworksCmd();
            return list.withNameFilter(newtworkName).toString();
        } catch (Exception e) {
            System.out.println("Faild to get the docker id " + e.getMessage());
            return null;
        }

    }

}