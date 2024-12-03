package com.rasysbox.ws.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsMemUsageLimitDTO {
    private String memUsage;
    private String memLimit;
}
