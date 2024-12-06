package com.rasysbox.ws.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EnvsDTO {
    private List<EnvVariable> variables;

    @Getter
    @Setter
    public static class EnvVariable {
        private String key;
        private String value;
    }
}
