package com.example.DockerNode.InitService;

import java.nio.file.Paths;
import java.util.*;

import com.example.DockerNode.ContainerManager.DockerService;

import com.github.dockerjava.api.command.CreateContainerResponse;

import com.github.dockerjava.api.model.*;

public class Init {
        static DockerService DS = new DockerService();

        public static void startBaseConatiner() {
                List<ExposedPort> ports = Arrays.asList(
                                ExposedPort.tcp(80), // TCP port 80
                                ExposedPort.tcp(8080) // TCP port 8080
                );
                Ports portBindings = new Ports();
                portBindings.bind(ExposedPort.tcp(80), Ports.Binding.bindPort(80));
                portBindings.bind(ExposedPort.tcp(8080), Ports.Binding.bindPort(8080));

                // 2. Create Binds object (correct type for withBinds)
                String traefikConfigPath = Paths.get("./traefik.yml").toAbsolutePath().normalize().toString();
                Binds binds = new Binds(
                                Bind.parse(traefikConfigPath + ":/etc/traefik/traefik.yml"), // Now uses absolute path
                                Bind.parse("/var/run/docker.sock:/var/run/docker.sock"));

                // 3. Create HostConfig
                HostConfig hostConfig = new HostConfig()
                                .withPortBindings(portBindings)
                                .withBinds(binds) // Now accepts Binds object
                                .withNetworkMode("DockerNodeNetwork");
                String[] cmd = {
                                "--api.insecure=true",
                                "--providers.docker",
                                "--providers.file.filename=/etc/traefik/traefik.yml",
                                "--log.level=DEBUG" // Helpful for debugging
                };

                CreateContainerResponse basecontainer = DS.dockerClient.createContainerCmd("traefik")
                                .withName("traefik")
                                .withAttachStderr(true)
                                .withAttachStdin(false)
                                .withAttachStdout(true)
                                .withTty(false)
                                .withExposedPorts(ports)
                                .withHostConfig(hostConfig)
                                .withStdinOpen(true)
                                .withCmd(cmd)
                                .exec();
                DS.startContainer(basecontainer.getId());
        }

        public static void StartInitService() {
                System.out.println("Init service is starting ...");
                DS.removeContainer("traefik");
                DS.deleteNetwork("DockerNodeNetwork");
                DS.createNetwork("DockerNodeNetwork");
                DS.pullImage("traefik:latest");
                startBaseConatiner();

        }
}
