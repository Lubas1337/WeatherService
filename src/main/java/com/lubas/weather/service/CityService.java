package com.lubas.weather.service;

import com.lubas.weather.dto.CityDto;
import com.lubas.weather.model.City;
import com.lubas.weather.repository.CityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CityService {

    private static final Logger logger = LoggerFactory.getLogger(CityService.class);
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public Page<CityDto> getAllCities(int page, int size) {
        logger.info("Fetching all cities with pagination - Page: {}, Size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<City> cities = cityRepository.findAll(pageable);

        logger.info("Fetched {} cities from database", cities.getTotalElements());
        return cities.map(this::convertToDto);
    }

    public CityDto updateCity(int cityId, CityDto cityDto) {
        logger.info("Updating city with ID: {}", cityId);

        return cityRepository.findById(cityId).map(city -> {
            city.setName(cityDto.getName());
            city.setCountry(cityDto.getCountry());
            city.setLatitude(cityDto.getLatitude());
            city.setLongitude(cityDto.getLongitude());
            city.setUpdatedAt(cityDto.getUpdatedAt());

            City updatedCity = cityRepository.save(city);
            logger.info("City updated successfully: {}", updatedCity);
            return convertToDto(updatedCity);
        }).orElseThrow(() -> {
            logger.error("City not found with ID: {}", cityId);
            return new RuntimeException("City not found with id: " + cityId);
        });
    }

    public void deleteCity(int cityId) {
        logger.info("Deleting city with ID: {}", cityId);

        if (cityRepository.existsById(cityId)) {
            cityRepository.deleteById(cityId);
            logger.info("City with ID: {} deleted successfully", cityId);
        } else {
            logger.error("City not found with ID: {}", cityId);
            throw new RuntimeException("City not found with id: " + cityId);
        }
    }

    private CityDto convertToDto(City city) {
        CityDto cityDto = new CityDto();
        cityDto.setId(city.getId());
        cityDto.setName(city.getName());
        cityDto.setCountry(city.getCountry());
        cityDto.setLatitude(city.getLatitude());
        cityDto.setLongitude(city.getLongitude());
        cityDto.setUpdatedAt(city.getUpdatedAt());
        return cityDto;
    }

    private City convertToEntity(CityDto cityDto) {
        City city = new City();
        city.setId(cityDto.getId());
        city.setName(cityDto.getName());
        city.setCountry(cityDto.getCountry());
        city.setLatitude(cityDto.getLatitude());
        city.setLongitude(cityDto.getLongitude());
        city.setUpdatedAt(cityDto.getUpdatedAt());
        return city;
    }
}
