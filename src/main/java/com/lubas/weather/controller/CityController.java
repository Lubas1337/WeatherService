package com.lubas.weather.controller;

import com.lubas.weather.dto.CityDto;
import com.lubas.weather.model.City;
import com.lubas.weather.service.CityService;
import com.lubas.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;
    private final WeatherService weatherService;

    @Autowired
    public CityController(CityService cityService, WeatherService weatherService) {
        this.cityService = cityService;
        this.weatherService = weatherService;
    }

    @PostMapping("/add")
    @Operation(summary = "Добавление города(Он должен существовать)")
    public Mono<City> getWeather(@RequestParam String city) {
        return weatherService.getCityWeather(city);
    }

    @GetMapping
    @Operation(summary = "Вывод всех добалвенных городов")
    public Page<CityDto> getAllCities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;

        return cityService.getAllCities(pageNumber, pageSize);
    }

    @PutMapping("/{cityId}")
    public CityDto updateCity(@PathVariable int cityId, @RequestBody CityDto cityDto) {
        return cityService.updateCity(cityId, cityDto);
    }

    @DeleteMapping("/{cityId}")
    public void deleteCity(@PathVariable int cityId) {
        cityService.deleteCity(cityId);
    }
}

