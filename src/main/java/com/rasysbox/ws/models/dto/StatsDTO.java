package com.rasysbox.ws.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsDTO {
    @JsonProperty("container_id")
    private String containerId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("name")
    private String name;

    @JsonProperty("cpu_%")
    private String cpuPercent;

    @JsonProperty("mem_usage_limit")
    private StatsMemUsageLimitDTO memUsageLimit;

    @JsonProperty("mem_%")
    private String memPercent;

    @JsonProperty("net_io")
    private StatsNetIoDTO netIO;

    @JsonProperty("block_io")
    private StatsBlockIoDTO blockIO;

    @JsonProperty("pids")
    private String pids;
}
