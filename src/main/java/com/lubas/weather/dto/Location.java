package com.lubas.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Location {

    private String name;
    private String region;
    private String country;
    private double lat;
    private double lon;

    @JsonProperty("tz_id")
    private String tzId;

    @JsonProperty("localtime_epoch")
    private long localtimeEpoch;

    private String localtime;
}
