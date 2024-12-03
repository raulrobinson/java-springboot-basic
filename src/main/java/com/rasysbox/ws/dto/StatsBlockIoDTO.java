package com.rasysbox.ws.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsBlockIoDTO {
    private String blockIn;
    private String blockOut;
}
