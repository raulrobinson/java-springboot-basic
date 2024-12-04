package com.rasysbox.ws.dto;

import lombok.*;

import java.util.HashMap;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateContainerDTO {
    private HashMap<String, String> envs;
    private String image;
    private HashMap<String, String> ports;

    public HashMap<String, String> getEnvs() {
        return envs;
    }
}
