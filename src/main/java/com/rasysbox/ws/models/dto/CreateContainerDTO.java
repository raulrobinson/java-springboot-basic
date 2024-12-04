package com.rasysbox.ws.models.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateContainerDTO {
    private String containerName;
    private PortsDTO ports;
    private EnvsDTO envs;
    private String image;
}
