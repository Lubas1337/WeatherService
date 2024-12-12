package com.lubas.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentWeatherDto {
    private String cityName;
    private double temperatureCelsius;
    private int humidity;
    private double pressureMb;
    private double windSpeedMph;
    private String conditionDescription;
}
