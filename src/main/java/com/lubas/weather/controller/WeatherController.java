package com.lubas.weather.controller;

import com.lubas.weather.dto.CurrentWeatherDto;
import com.lubas.weather.dto.WeatherResponseDto;
import com.lubas.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather/current")
    public CurrentWeatherDto getCurrentWeather(@RequestParam int cityId) {
        return weatherService.getCurrentWeatherByCityId(cityId);
    }

    @GetMapping("/weather/forecast")
    public WeatherResponseDto getForecast(
            @RequestParam String cityName,
            @RequestParam int days
    ) {
        return weatherService.getCurrentForecast(cityName, days);
    }
}
