package com.lubas.weather.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityResponse {

    private int id;
    private String name;
    private String country;
    private double lat;
    private double lon;

    public CityResponse() {}

    @JsonCreator
    public CityResponse(@JsonProperty("id") int id,
                        @JsonProperty("name") String name,
                        @JsonProperty("country") String country,
                        @JsonProperty("lat") double lat,
                        @JsonProperty("lon") double lon) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
    }

}
