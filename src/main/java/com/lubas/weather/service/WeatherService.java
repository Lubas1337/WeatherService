package com.lubas.weather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lubas.weather.client.WeatherClient;
import com.lubas.weather.dto.*;
import com.lubas.weather.model.City;
import com.lubas.weather.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WeatherClient weatherClient;
    private final CityRepository cityRepository;

    @Autowired
    public WeatherService(WeatherClient weatherClient, CityRepository cityRepository) {
        this.weatherClient = weatherClient;
        this.cityRepository = cityRepository;
    }

    public Mono<City> getCityWeather(String cityName) {
        logger.info("Fetching weather data for city: {}", cityName);

        Map<String, Object> params = new HashMap<>();
        params.put("q", cityName);

        return weatherClient.makeRequest("/search.json", params).publishOn(Schedulers.boundedElastic()).mapNotNull(response -> {
            CityResponse[] cityResponses = parseWeatherResponse(response);

            if (cityResponses != null && cityResponses.length > 0) {
                CityResponse cityResponse = cityResponses[0];
                logger.debug("City response received: {}", cityResponse);

                Optional<City> existingCityOpt = cityRepository.findByName(cityResponse.getName());
                City city;
                if (existingCityOpt.isPresent()) {
                    city = existingCityOpt.get();
                    logger.debug("Updating existing city: {}", city.getName());
                    city.setCountry(cityResponse.getCountry());
                    city.setLatitude(cityResponse.getLat());
                    city.setLongitude(cityResponse.getLon());
                    city.setUpdatedAt(LocalDateTime.now());
                } else {
                    city = new City();
                    logger.debug("Creating new city: {}", cityResponse.getName());
                    city.setName(cityResponse.getName());
                    city.setCountry(cityResponse.getCountry());
                    city.setLatitude(cityResponse.getLat());
                    city.setLongitude(cityResponse.getLon());
                }

                cityRepository.save(city);
                logger.info("City saved successfully: {}", city);
                return city;
            }

            logger.warn("No city data found for: {}", cityName);
            return null;
        }).doOnError(e -> logger.error("Error fetching weather data for city: {}", cityName, e));
    }

    public CurrentWeatherDto getCurrentWeatherByCityId(int cityId) {
        logger.info("Fetching current weather for city ID: {}", cityId);

        City city = cityRepository.findById(cityId).orElseThrow(() -> {
            logger.error("City not found with ID: {}", cityId);
            return new RuntimeException("City not found with id: " + cityId);
        });

        WeatherResponse response = weatherClient.makeRequestForCurrent("/current.json", Map.of("q", city.getName())).block();

        assert response != null;
        logger.debug("Weather response received for city: {}", city.getName());
        return mapToCurrentWeatherDto(response, city.getName());
    }

    public WeatherResponseDto getCurrentForecast(String cityName, int days) {
        return weatherClient.makeRequestForForecast("/forecast.json",
                Map.of("q", cityName, "days", days)).block();
    }


    private CurrentWeatherDto mapToCurrentWeatherDto(WeatherResponse response, String cityName) {
        CurrentWeather current = response.getCurrent();
        logger.debug("Mapping weather data to DTO for city: {}", cityName);
        return new CurrentWeatherDto(cityName, current.getTempC(), current.getHumidity(), current.getPressureMb(), current.getWindMph(), current.getCondition().getText());
    }

    private CityResponse[] parseWeatherResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            logger.debug("Parsing weather response");
            return objectMapper.readValue(response, CityResponse[].class);
        } catch (Exception e) {
            logger.error("Error parsing weather response", e);
            return null;
        }
    }
}
