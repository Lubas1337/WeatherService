package com.lubas.weather.dto;

import lombok.Data;

@Data
public class WeatherResponse {
    private Location location;
    private CurrentWeather current;
}