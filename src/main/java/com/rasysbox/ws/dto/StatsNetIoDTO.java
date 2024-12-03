package com.rasysbox.ws.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsNetIoDTO {
    private String netIn;
    private String netOut;
}
