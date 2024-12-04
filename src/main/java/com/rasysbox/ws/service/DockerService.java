package com.rasysbox.ws.service;

import com.rasysbox.ws.models.dto.CreateContainerDTO;
import com.rasysbox.ws.models.dto.StatsDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DockerService {
    List<Map<String, String>> listContainers();

    List<Map<String, String>> getContainerByName(String containerName);

    List<Map<String, String>> getContainerLogs(String containerId);

    List<Map<String, String>> stopContainer(String containerId);

    List<Map<String, String>> startContainer(String containerId);

    List<StatsDTO> getContainerStats(String containerId);

    List<Map<String, String>> removeContainer(String containerId) throws IOException;

    List<Map<String, String>> createContainer(CreateContainerDTO request);

    List<Map<String, String>> pushImage(String image);

    List<Map<String, String>> pullImage(String image);

    List<Map<String, String>> listImages();

    List<Map<String, String>> removeImage(String image);
}
