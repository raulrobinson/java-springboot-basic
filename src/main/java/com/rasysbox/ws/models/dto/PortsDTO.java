package com.rasysbox.ws.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class PortsDTO {
    private List<PortMapping> portMappings;

    @Getter
    @Setter
    public static class PortMapping {
        private int hostPort;
        private int containerPort;
    }
}
