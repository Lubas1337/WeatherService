package com.lubas.weather.client;

import com.lubas.weather.config.properties.WeatherProperties;
import com.lubas.weather.dto.WeatherResponse;
import com.lubas.weather.dto.WeatherResponseDto;
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
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(endpoint);
                    params.forEach(uriBuilder::queryParam);
                    uriBuilder.queryParam("key", weatherProperties.getToken());
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(this::handleError);
    }

    public Mono<WeatherResponse> makeRequestForCurrent(String endpoint, Map<String, Object> params) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(endpoint);
                    params.forEach(uriBuilder::queryParam);
                    uriBuilder.queryParam("key", weatherProperties.getToken());
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .onErrorResume(this::handleError);
    }

    public Mono<WeatherResponseDto> makeRequestForForecast(String endpoint, Map<String, Object> params) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(endpoint);
                    params.forEach(uriBuilder::queryParam);
                    uriBuilder.queryParam("key", weatherProperties.getToken());
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(WeatherResponseDto.class)
                .onErrorResume(this::handleError);
    }

    private <T> Mono<T> handleError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException webClientResponseException) {
            return switch (webClientResponseException.getStatusCode()) {
                case UNAUTHORIZED -> Mono.error(new RuntimeException("Authorization error: Invalid API key"));
                case SERVICE_UNAVAILABLE ->
                        Mono.error(new RuntimeException("Weather API is unavailable, please try again later"));
                default ->
                        Mono.error(new RuntimeException("Error response from WeatherAPI: " + webClientResponseException.getMessage()));
            };
        } else if (throwable instanceof ConnectException) {
            return Mono.error(new RuntimeException("Unable to connect to Weather API. Check your network or API server status."));
        } else {
            return Mono.error(new RuntimeException("Unexpected error: " + throwable.getMessage()));
        }
    }
}
