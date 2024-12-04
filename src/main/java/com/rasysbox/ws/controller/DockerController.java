package com.rasysbox.ws.controller;

import com.rasysbox.ws.models.dto.CreateContainerDTO;
import com.rasysbox.ws.models.dto.StatsDTO;
import com.rasysbox.ws.service.DockerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "Docker", value = "Docker API")
@RequestMapping(path = "${controller.properties.base-path}/docker", produces = MediaType.APPLICATION_JSON_VALUE)
public class DockerController {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(DockerController.class);

    private final DockerService service;

    @Autowired
    public DockerController(DockerService service) {
        this.service = service;
    }

    @GetMapping("/containers")
    @ApiOperation(value = "List all containers", notes = "List all containers")
    public ResponseEntity<List<Map<String, String>>> getContainers() {
        var containers = service.listContainers();
        if (containers.isEmpty()) {
            logger.info("No containers found, Docker is empty or not running");
            return ResponseEntity.noContent().build();
        }
        logger.info("Getting total {} containers", containers.size());
        return ResponseEntity.ok(containers);
    }

    @GetMapping("/name/{containerName}")
    @ApiOperation(value = "Get container by name", notes = "Get container by name")
    public ResponseEntity<List<Map<String, String>>> getContainerByName(@PathVariable String containerName) {
        var container = service.getContainerByName(containerName);
        if (container.isEmpty()) {
            logger.info("Container name {} not found", containerName);
            return ResponseEntity.notFound().build();
        }
        logger.info("Getting container {}", containerName);
        return ResponseEntity.ok(container);
    }

    @GetMapping("/logs/{containerId}")
    @ApiOperation(value = "Get container logs", notes = "Get container logs")
    public ResponseEntity<List<Map<String, String>>> getContainerLogs(@PathVariable String containerId) {
        var logs = service.getContainerLogs(containerId);
        if (logs.isEmpty()) {
            logger.info("No logs found for container {}", containerId);
            return ResponseEntity.noContent().build();
        }
        logger.info("Getting logs for container {}", containerId);
        return ResponseEntity.ok(logs);
    }

    @PostMapping("/stop/{containerId}")
    @ApiOperation(value = "Stop container", notes = "Stop container")
    public ResponseEntity<List<Map<String, String>>> stopContainer(@PathVariable String containerId) {
        var result = service.stopContainer(containerId);
        if (result.isEmpty()) {
            logger.info("Container id for stop {} not found", containerId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Container {} stopped", containerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/start/{containerId}")
    @ApiOperation(value = "Start container", notes = "Start container")
    public ResponseEntity<List<Map<String, String>>> startContainer(@PathVariable String containerId) {
        var result = service.startContainer(containerId);
        if (result.isEmpty()) {
            logger.info("Container id for start {} not found", containerId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Container {} started", containerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/stats/{containerId}")
    @ApiOperation(value = "Get container stats", notes = "Get container stats")
    public ResponseEntity<List<StatsDTO>> getContainerStats(@PathVariable String containerId) {
        var stats = service.getContainerStats(containerId);
        if (stats.isEmpty()) {
            logger.info("No stats found for container {}", containerId);
            return ResponseEntity.noContent().build();
        }
        logger.info("Getting stats for container {}", containerId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/images")
    @ApiOperation(value = "List all images", notes = "List all images")
    public ResponseEntity<List<Map<String, String>>> getImages() {
        var images = service.listImages();
        if (images.isEmpty()) {
            logger.info("No images found, Docker is empty or not running");
            return ResponseEntity.noContent().build();
        }
        logger.info("Getting total {} images", images.size());
        return ResponseEntity.ok(images);
    }

    @PostMapping("/create-container")
    @ApiOperation(value = "Create container", notes = "Create container")
    public ResponseEntity<List<Map<String, String>>> createContainer(@RequestBody CreateContainerDTO request) {
        var result = service.createContainer(request);
        if (result.isEmpty()) {
            logger.info("Container {} not created", request.getContainerName());
            return ResponseEntity.notFound().build();
        }
        logger.info("Container {} created", request.getContainerName());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/remove/{containerId}")
    @ApiOperation(value = "Remove container", notes = "Remove container")
    public ResponseEntity<List<Map<String, String>>> removeContainer(@PathVariable String containerId) throws IOException {
        var result = service.removeContainer(containerId);
        if (result.isEmpty()) {
            logger.info("Container id for remove {} not found", containerId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Container {} removed", containerId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/remove-image/{image_id}")
    @ApiOperation(value = "Remove image", notes = "Remove image")
    public ResponseEntity<List<Map<String, String>>> removeImage(@PathVariable String image_id) throws IOException {
        var result = service.removeImage(image_id);
        if (result.isEmpty()) {
            logger.info("Image {} not removed", image_id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Image {} removed", image_id);
        return ResponseEntity.ok(result);
    }
}
