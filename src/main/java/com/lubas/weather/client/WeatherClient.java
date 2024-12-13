package com.lubas.weather.client;

import com.lubas.weather.config.properties.WeatherProperties;
import com.lubas.weather.dto.WeatherResponse;
import com.lubas.weather.dto.WeatherResponseDto;
import com.lubas.weather.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Map;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class WeatherClient {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WebClient webClient;
    private final WeatherProperties weatherProperties;

    @Autowired
    public WeatherClient(WeatherProperties weatherProperties) {
        this.weatherProperties = weatherProperties;
        this.webClient = WebClient.builder()
                .baseUrl(weatherProperties.getApiUrl())
                .defaultHeader("accept", "application/json")
                .build();
    }

    public Mono<String> makeRequest(String endpoint, Map<String, Object> params) {
        return executeRequest(endpoint, params, String.class);
    }

    public Mono<WeatherResponse> makeRequestForCurrent(String endpoint, Map<String, Object> params) {
        logger.info(endpoint + params);
        return executeRequest(endpoint, params, WeatherResponse.class);
    }

    public Mono<WeatherResponseDto> makeRequestForForecast(String endpoint, Map<String, Object> params) {
        logger.info(endpoint + params);
        return executeRequest(endpoint, params, WeatherResponseDto.class);
    }

    private <T> Mono<T> executeRequest(String endpoint, Map<String, Object> params, Class<T> responseType) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(endpoint);
                    params.forEach(uriBuilder::queryParam);
                    uriBuilder.queryParam("key", weatherProperties.getToken());
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(this::handleError);
    }

    private <T> Mono<T> handleError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException webClientResponseException) {
            return switch (webClientResponseException.getStatusCode()) {
                case UNAUTHORIZED -> Mono.error(new RuntimeException("Authorization error: Invalid API key"));
                case SERVICE_UNAVAILABLE -> Mono.error(new RuntimeException("Weather API is unavailable, please try again later"));
                default -> Mono.error(new RuntimeException("Error response from Weather API: " + webClientResponseException.getMessage()));
            };
        } else if (throwable instanceof ConnectException) {
            return Mono.error(new RuntimeException("Unable to connect to Weather API. Check your network or API server status."));
        } else {
            return Mono.error(new RuntimeException("Unexpected error: " + throwable.getMessage()));
        }
    }
}
