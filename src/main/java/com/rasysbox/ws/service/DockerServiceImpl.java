package com.rasysbox.ws.service;

import com.rasysbox.ws.models.dto.*;
import com.rasysbox.ws.utils.ExecutorCommand;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static com.rasysbox.ws.utils.Utilities.generateIsoTimestamp;

@Service
public class DockerServiceImpl implements DockerService {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(DockerServiceImpl.class);

    private final ExecutorCommand executorCommand;

    @Autowired
    public DockerServiceImpl(ExecutorCommand executorCommand) {
        this.executorCommand = executorCommand;
    }

    @Override
    public List<Map<String, String>> listContainers() {
        List<Map<String, String>> containers = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("docker", "ps", "-a").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            String[] headers = null;
            if ((line = reader.readLine()) != null) {
                headers = line.split("\\s{2,}");
                // Convertir encabezados a minúsculas y reemplazar espacios por _
                for (int i = 0; i < headers.length; i++) {
                    headers[i] = headers[i].trim().toLowerCase().replace(" ", "_");
                }
            }

            // Leer las siguientes líneas como datos
            while ((line = reader.readLine()) != null && headers != null) {
                String[] values = line.split("\\s{2,}");
                Map<String, String> container = new HashMap<>();
                String names = null; // Variable para almacenar nombres
                String ports = null; // Variable para almacenar puertos
                boolean isPortsAssigned = false; // Controlador para asegurar que ports se asigne correctamente

                // Asignar valores a las columnas
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    String key = headers[i];
                    String value = values[i].trim();

                    // Si el campo es "status", separar "status" y "since_time"
                    if (key.equals("status")) {
                        // Verificamos si el campo "status" contiene unidades de tiempo
                        String[] statusParts = value.split(" ", 2);
                        container.put("status", statusParts[0].toLowerCase()); // Asignar solo la primera parte como "status"
                        if (statusParts.length > 1) {
                            container.put("since_time", statusParts[1]); // La segunda parte va en "since_time"
                        }
                    }

                    // Si el campo es "ports", asignar solo si tiene un valor y no está superponiendo el campo "names"
                    else if (key.equals("ports") && !value.isEmpty() && !value.contains("tcp") && !value.contains(":")) {
                        // Si no tiene formato de puerto (ej. no contiene ":")
                        names = value;  // Asignar como "names" en caso de no ser puerto
                    } else if (key.equals("ports") && !value.isEmpty()) {
                        // Si el campo tiene formato de puerto, asignarlo a "ports"
                        ports = value;
                        isPortsAssigned = true;
                    }

                    // Si es el campo "names", guardarlo en la variable names
                    else if (key.equals("names")) {
                        names = value;
                    } else {
                        container.put(key, value); // Otros campos directamente
                    }
                }

                // Agregar 'names' solo si tiene un valor
                if (names != null) {
                    container.put("names", names);
                }

                // Si no se asignó "ports", no agregarlo
                if (isPortsAssigned) {
                    container.put("ports", ports);
                } else {
//                    container.remove("ports");
                    container.put("ports", "null");
                }

                // Agregar la marca de tiempo
                container.put("timestamp", generateIsoTimestamp());

                containers.add(container);
            }

        } catch (Exception e) {
            logger.error("Error al listar los contenedores", e);
        }

        return containers;
    }

    @Override
    public List<Map<String, String>> getContainerByName(String containerName) {
        try {
            Process process = new ProcessBuilder("docker", "ps", "-a", "-q", "-f", "name=" + containerName).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String containerId = reader.readLine();
            var command = executorCommand.executeCommand("docker inspect " + containerId);
            List<Map<String, String>> containers = new ArrayList<>();
            HashMap<String, String> container = new HashMap<>();
            container.put("container_id", containerId);
            container.put("container_name", containerName);
            container.put("status", "created");
            container.put("timestamp", generateIsoTimestamp());
            containers.add(container);
            return containers;
        } catch (Exception e) {
            logger.error("Error al obtener el contenedor por nombre {}", containerName, e);
            return null;
        }
    }

    @Override
    public List<Map<String, String>> getContainerLogs(String containerId) {
        try {
            var command = executorCommand.executeCommand("docker logs " + containerId);
            List<Map<String, String>> logs = new ArrayList<>();
            HashMap<String, String> log = new HashMap<>();
            log.put("container_id", containerId);
            log.put("logs", command);
            log.put("timestamp", generateIsoTimestamp());
            logs.add(log);
            return logs;
        } catch (Exception e) {
            logger.error("Error al obtener los logs del contenedor {}", containerId, e);
            return null;
        }
    }

    @Override
    public List<Map<String, String>> stopContainer(String containerId) {
        try {
            Process process = new ProcessBuilder("docker", "stop", containerId).start();
            process.waitFor();
            List<Map<String, String>> containers = new ArrayList<>();
            HashMap<String, String> container = new HashMap<>();
            container.put("container_id", containerId);
            container.put("status", "exited");
            container.put("timestamp", generateIsoTimestamp());
            containers.add(container);
            return containers;
        } catch (Exception e) {
            logger.error("Error al detener el contenedor {}", containerId, e);
            return null;
        }
    }

    @Override
    public List<Map<String, String>> startContainer(String containerId) {
        try {
            Process process = new ProcessBuilder("docker", "start", containerId).start();
            process.waitFor();
            List<Map<String, String>> containers = new ArrayList<>();
            HashMap<String, String> container = new HashMap<>();
            container.put("container_id", containerId);
            container.put("status", "up");
            container.put("timestamp", generateIsoTimestamp());
            containers.add(container);
            return containers;
        } catch (Exception e) {
            logger.error("Error al iniciar el contenedor {}", containerId, e);
            return null;
        }
    }

    @Override
    public List<StatsDTO> getContainerStats(String containerId) {
        try {
            var command = executorCommand.executeCommand("docker stats --no-stream " + containerId);

            // Split stats into lines
            String[] lines = command.split("\r\n");
            if (lines.length < 2) {
                throw new IllegalArgumentException("Formato inesperado de estadísticas para el contenedor: " + containerId);
            }

            // Extract headers and values
            String[] headers = lines[0].trim().split("\\s{2,}");
            String[] values = lines[1].trim().split("\\s{2,}");

            // Convert stats to a structured JSON object with normalized keys
            Map<String, Object> statsJson = new LinkedHashMap<>();
            for (int i = 0; i < headers.length; i++) {
                // Normalize keys: replace spaces with underscores, remove "/", and replace "_/" with "_"
                String normalizedKey = headers[i].toLowerCase()
                        .replace(" ", "_") // Replace spaces with underscores
                        .replace("_/", "") // Replace "_/" with ""
                        .replace("/", ""); // Remove slashes

                String value = i < values.length ? values[i] : "";

                switch (normalizedKey) {
                    case "mem_usage_limit":
                        String[] memParts = value.split("/");
                        Map<String, String> memUsageLimit = new LinkedHashMap<>();
                        memUsageLimit.put("mem_usage", memParts.length > 0 ? memParts[0].trim() : null);
                        memUsageLimit.put("mem_limit", memParts.length > 1 ? memParts[1].trim() : null);
                        statsJson.put("mem_usage_limit", memUsageLimit);
                        break;
                    case "net_io":
                        String[] netParts = value.split("/");
                        Map<String, String> netIO = new LinkedHashMap<>();
                        netIO.put("rx_bytes", netParts.length > 0 ? netParts[0].trim() : null);
                        netIO.put("tx_bytes", netParts.length > 1 ? netParts[1].trim() : null);
                        statsJson.put("net_io", netIO);
                        break;
                    case "block_io":
                        String[] blockParts = value.split("/");
                        Map<String, String> blockIO = new LinkedHashMap<>();
                        blockIO.put("block_io_in", blockParts.length > 0 ? blockParts[0].trim() : null);
                        blockIO.put("block_io_out", blockParts.length > 1 ? blockParts[1].trim() : null);
                        statsJson.put("block_io", blockIO);
                        break;
                    default:
                        statsJson.put(normalizedKey, value);
                        break;
                }
            }
            statsJson.put("timestamp", generateIsoTimestamp());

            return List.of(StatsDTO.builder()
                            .containerId((String) statsJson.get("container_id"))
                            .name((String) statsJson.get("name"))
                            .cpuPercent((String) statsJson.get("cpu_%"))
                            .memPercent((String) statsJson.get("mem_%"))
                            .memUsageLimit(
                                    StatsMemUsageLimitDTO.builder()
                                            .memUsage(((Map<String, String>) statsJson.get("mem_usage_limit")).get("mem_usage"))
                                            .memLimit(((Map<String, String>) statsJson.get("mem_usage_limit")).get("mem_limit"))
                                            .build()
                            )
                            .timestamp((String) statsJson.get("timestamp"))
                            .netIO(
                                    StatsNetIoDTO.builder()
                                            .netIn(((Map<String, String>) statsJson.get("net_io")).get("rx_bytes"))
                                            .netOut(((Map<String, String>) statsJson.get("net_io")).get("tx_bytes"))
                                            .build()
                            )
                            .blockIO(
                                    StatsBlockIoDTO.builder()
                                            .blockIn(((Map<String, String>) statsJson.get("block_io")).get("block_io_in"))
                                            .blockOut(((Map<String, String>) statsJson.get("block_io")).get("block_io_out"))
                                            .build()
                            )
                            .pids((String) statsJson.get("pids"))
                    .build());

        } catch (Exception e) {
            logger.error("Error al obtener las estadísticas del contenedor {}", containerId, e);
            return null;
        }
    }

    @Override
    public List<Map<String, String>> createContainer(CreateContainerDTO request) {
        try {
            StringBuilder command = new StringBuilder("docker run -d --name " + request.getContainerName());

            // Add ports mapping.
            for (PortsDTO.PortMapping portMapping : request.getPorts().getPortMappings()) {
                command.append(" -p ")
                        .append(portMapping.getHostPort())
                        .append(":")
                        .append(portMapping.getContainerPort());
            }

            // Add environment variables.
            for (EnvsDTO.EnvVariable variable : request.getEnvs().getVariables()) {
                command.append(" -e ")
                        .append(variable.getKey())
                        .append("=")
                        .append(variable.getValue());
            }

            // Add image name.
            command.append(" ").append(request.getImage());

            return executorCommand.createDockerContainer(command.toString());
        } catch (Exception e) {
            logger.error("Error al crear el contenedor de la imagen {}", request.getImage(), e);
            return null;
        }
    }

    @Override
    public List<Map<String, String>> removeContainer(String containerId) throws IOException {
        String command = "docker rm -f " + containerId;
        Process process = Runtime.getRuntime().exec(command);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String result = reader.readLine();
            List<Map<String, String>> containers = new ArrayList<>();
            HashMap<String, String> container = new HashMap<>();
            if (result != null && !result.isEmpty()) {
                container.put("container_id", containerId);
                container.put("status", "removed");
                container.put("timestamp", generateIsoTimestamp());
                containers.add(container);
                return containers;
            } else {
                container.put("status", "container not found");
                container.put("timestamp", generateIsoTimestamp());
                containers.add(container);
                return containers;
            }
        } catch (Exception e) {
            logger.error("Error al eliminar el contenedor {}", containerId, e);
            return null;
        }
    }

    @Override
    public List<Map<String, String>> listImages() {
        List<Map<String, String>> images = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("docker", "images").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            String[] headers = null;
            if ((line = reader.readLine()) != null) {
                headers = line.split("\\s{2,}");
                // Convertir encabezados a minúsculas y reemplazar espacios por _
                for (int i = 0; i < headers.length; i++) {
                    headers[i] = headers[i].trim().toLowerCase().replace(" ", "_");
                }
            }

            // Leer las siguientes líneas como datos
            while ((line = reader.readLine()) != null && headers != null) {
                String[] values = line.split("\\s{2,}");
                Map<String, String> image = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    String key = headers[i];
                    String value = values[i].trim();
                    image.put("timestamp", generateIsoTimestamp());
                    image.put(key, value);
                }
                images.add(image);
            }

        } catch (Exception e) {
            logger.error("Error al listar las imágenes", e);
        }

        return images;
    }

    @Override
    public List<Map<String, String>> removeImage(String image_id) throws IOException {
        String command = "docker rmi " + image_id;
        Process process = Runtime.getRuntime().exec(command);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String result = reader.readLine();
            List<Map<String, String>> containers = new ArrayList<>();
            HashMap<String, String> container = new HashMap<>();
            if (result != null && !result.isEmpty()) {
                container.put("image", image_id);
                container.put("status", "removed");
                container.put("timestamp", generateIsoTimestamp());
                containers.add(container);
                return containers;
            } else {
                container.put("status", "image not found");
                container.put("timestamp", generateIsoTimestamp());
                containers.add(container);
                return containers;
            }
        } catch (Exception e) {
            logger.error("Error al eliminar la imagen {}", image_id, e);
            return null;
        }
    }

    // ----------------------------------------------------------------------------------------------------

    @Override
    public List<Map<String, String>> pushImage(String image) {
        try {
            Process process = new ProcessBuilder("docker", "push", image).start();
            process.waitFor();
            List<Map<String, String>> images = new ArrayList<>();
            HashMap<String, String> img = new HashMap<>();
            img.put("image", image);
            img.put("status", "pushed");
            img.put("timestamp", generateIsoTimestamp());
            images.add(img);
            return images;
        } catch (Exception e) {
            logger.error("Error al subir la imagen {}", image, e);
            return null;
        }
    }

    @Override
    public List<Map<String, String>> pullImage(String image) {
        try {
            Process process = new ProcessBuilder("docker", "pull", image).start();
            process.waitFor();
            List<Map<String, String>> images = new ArrayList<>();
            HashMap<String, String> img = new HashMap<>();
            img.put("image", image);
            img.put("status", "pulled");
            img.put("timestamp", generateIsoTimestamp());
            images.add(img);
            return images;
        } catch (Exception e) {
            logger.error("Error al descargar la imagen {}", image, e);
            return null;
        }
    }
}
