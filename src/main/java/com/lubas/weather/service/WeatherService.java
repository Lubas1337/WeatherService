package com.lubas.weather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lubas.weather.client.WeatherClient;
import com.lubas.weather.dto.*;
import com.lubas.weather.model.City;
import com.lubas.weather.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;



    @Autowired
    public WeatherService(WeatherClient weatherClient, CityRepository cityRepository, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.weatherClient = weatherClient;
        this.cityRepository = cityRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
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
        String redisKey = "weather:current:" + cityId;

        String cachedData = redisTemplate.opsForValue().get(redisKey);
        if (cachedData != null) {
            logger.info("Returning cached current weather for city ID: {}", cityId);
            return parseCurrentWeatherFromCache(cachedData);
        }

        logger.info("Fetching current weather for city ID: {}", cityId);

        City city = cityRepository.findById(cityId).orElseThrow(() -> {
            logger.error("City not found with ID: {}", cityId);
            return new RuntimeException("City not found with ID: " + cityId);
        });

        WeatherResponse response = weatherClient.makeRequestForCurrent("/current.json", Map.of("q", city.getName())).block();

        assert response != null;
        logger.debug("Weather response received for city: {}", city.getName());
        CurrentWeatherDto weatherDto = mapToCurrentWeatherDto(response, city.getName());

        cacheData(redisKey, weatherDto);

        return weatherDto;
    }

    public WeatherResponseDto getCurrentForecast(int cityId, int days) {
        String redisKey = "weather:forecast:" + cityId + ":" + days;

        City city = cityRepository.findById(cityId).orElseThrow(() -> {
            logger.error("City not found with ID: {}", cityId);
            return new RuntimeException("City not found with ID: " + cityId);
        });

        String cachedData = redisTemplate.opsForValue().get(redisKey);
        if (cachedData != null) {
            logger.info("Returning cached forecast for city: {}", cityId);
            return parseForecastFromCache(cachedData);
        }

        logger.info("Fetching forecast for city: {} for {} days", cityId, days);

        WeatherResponseDto forecast = weatherClient.makeRequestForForecast("/forecast.json",
                Map.of("q", city.getName(), "days", days)).block();

        assert forecast != null;
        cacheData(redisKey, forecast);

        return forecast;
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

    private void cacheData(String key, Object data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, jsonData);
            redisTemplate.expireAt(key, LocalDateTime.now().toLocalDate().atStartOfDay().plusDays(1)
                    .atZone(ZoneId.systemDefault()).toInstant());
            logger.info("Cached data with key: {}", key);
        } catch (Exception e) {
            logger.error("Error caching data for key: {}", key, e);
        }
    }

    private CurrentWeatherDto parseCurrentWeatherFromCache(String cachedData) {
        try {
            return objectMapper.readValue(cachedData, CurrentWeatherDto.class);
        } catch (Exception e) {
            logger.error("Error parsing cached current weather data", e);
            throw new RuntimeException("Error parsing cached current weather data");
        }
    }

    private WeatherResponseDto parseForecastFromCache(String cachedData) {
        try {
            return objectMapper.readValue(cachedData, WeatherResponseDto.class);
        } catch (Exception e) {
            logger.error("Error parsing cached forecast data", e);
            throw new RuntimeException("Error parsing cached forecast data");
        }
    }
}
