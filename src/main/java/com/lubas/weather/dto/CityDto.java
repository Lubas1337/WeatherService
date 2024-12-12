package com.lubas.weather.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CityDto {
    private Integer id;
    private String name;
    private String country;
    private Double latitude;
    private Double longitude;
    private LocalDateTime updatedAt;
}

