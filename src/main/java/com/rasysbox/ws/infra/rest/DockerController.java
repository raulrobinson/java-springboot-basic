package com.rasysbox.ws.infra.rest;

import com.rasysbox.ws.domain.dto.ContainerDTO;
import com.rasysbox.ws.domain.dto.CreateContainerDTO;
import com.rasysbox.ws.domain.dto.ImageDTO;
import com.rasysbox.ws.domain.dto.StatsDTO;
import com.rasysbox.ws.domain.service.DockerService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
//@Api(tags = "Docker", value = "Docker API")
@Tag(name = "Docker", description = "Docker API")
@RequestMapping(path = "${controller.properties.base-path}/docker", produces = MediaType.APPLICATION_JSON_VALUE)
public class DockerController {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(DockerController.class);

    private final DockerService service;

    @Autowired
    public DockerController(DockerService service) {
        this.service = service;
    }

    @GetMapping("/containers")
//    @ApiOperation(value = "List all containers", notes = "List all containers")
    @Operation(summary = "List all containers", description = "List all containers")
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
//    @ApiOperation(value = "Get container by name", notes = "Get container by name")
    @Operation(summary = "Get container by name", description = "Get container by name")
    public ResponseEntity<List<Map<String, String>>> getContainerByName(@PathVariable String containerName) {
        var container = service.getContainerByName(containerName);
        if (container.isEmpty()) {
            logger.info("Container name {} not found", containerName);
            return ResponseEntity.notFound().build();
        }
        logger.info("Getting container {}", containerName);
        return ResponseEntity.ok(container);
    }

    @PostMapping("/logs")
//    @ApiOperation(value = "Get container logs", notes = "Get container logs")
    @Operation(summary = "Get container logs", description = "Get container logs")
    public ResponseEntity<List<Map<String, String>>> getContainerLogs(@RequestBody ContainerDTO container) {
        var logs = service.getContainerLogs(container.getContainerId());
        if (logs.isEmpty()) {
            logger.info("No logs found for container {}", container.getContainerId());
            return ResponseEntity.noContent().build();
        }
        logger.info("Getting logs for container {}", container.getContainerId());
        return ResponseEntity.ok(logs);
    }

    @PostMapping("/stop")
//    @ApiOperation(value = "Stop container", notes = "Stop container")
    @Operation(summary = "Stop container", description = "Stop container")
    public ResponseEntity<List<Map<String, String>>> stopContainer(@RequestBody ContainerDTO container) {
        var result = service.stopContainer(container.getContainerId());
        if (result.isEmpty()) {
            logger.info("Container id for stop {} not found", container.getContainerId());
            return ResponseEntity.notFound().build();
        }
        logger.info("Container {} stopped", container.getContainerId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/start")
//    @ApiOperation(value = "Start container", notes = "Start container")
    @Operation(summary = "Start container", description = "Start container")
    public ResponseEntity<List<Map<String, String>>> startContainer(@RequestBody ContainerDTO container) {
        var result = service.startContainer(container.getContainerId());
        if (result.isEmpty()) {
            logger.info("Container id for start {} not found", container.getContainerId());
            return ResponseEntity.notFound().build();
        }
        logger.info("Container {} started", container.getContainerId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/stats")
//    @ApiOperation(value = "Get container stats", notes = "Get container stats")
    @Operation(summary = "Get container stats", description = "Get container stats")
    public ResponseEntity<List<StatsDTO>> getContainerStats(@RequestBody() ContainerDTO container) {
        var stats = service.getContainerStats(container.getContainerId());
        if (stats.isEmpty()) {
            logger.info("No stats found for container {}", container.getContainerId());
            return ResponseEntity.noContent().build();
        }
        logger.info("Getting stats for container {}", container.getContainerId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/images")
//    @ApiOperation(value = "List all images", notes = "List all images")
    @Operation(summary = "List all images", description = "List all images")
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
//    @ApiOperation(value = "Create container", notes = "Create container")
    @Operation(summary = "Create container", description = "Create container")
    public ResponseEntity<List<Map<String, String>>> createContainer(@RequestBody() CreateContainerDTO request) {
        var result = service.createContainer(request);
        if (result.isEmpty()) {
            logger.info("Container {} not created", request.getContainerName());
            return ResponseEntity.notFound().build();
        }
        logger.info("Container {} created", request.getContainerName());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/remove")
//    @ApiOperation(value = "Remove container", notes = "Remove container")
    @Operation(summary = "Remove container", description = "Remove container")
    public ResponseEntity<List<Map<String, String>>> removeContainer(@RequestBody ContainerDTO container) throws IOException {
        var result = service.removeContainer(container.getContainerId());
        if (result.isEmpty()) {
            logger.info("Container id for remove {} not found", container.getContainerId());
            return ResponseEntity.notFound().build();
        }
        logger.info("Container {} removed", container.getContainerId());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/remove-image")
//    @ApiOperation(value = "Remove image", notes = "Remove image")
    @Operation(summary = "Remove image", description = "Remove image")
    public ResponseEntity<List<Map<String, String>>> removeImage(@RequestBody ImageDTO image) throws IOException {
        var result = service.removeImage(image.getImageId());
        if (result.isEmpty()) {
            logger.info("Image {} not removed", image.getImageId());
            return ResponseEntity.notFound().build();
        }
        logger.info("Image {} removed", image.getImageId());
        return ResponseEntity.ok(result);
    }
}
