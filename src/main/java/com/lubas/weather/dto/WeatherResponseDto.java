package com.lubas.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class WeatherResponseDto {

    private LocationDto location;
    private CurrentWeatherDto current;
    private ForecastDto forecast;

    @Setter
    @Getter
    public static class LocationDto {
        private String name;
        private String region;
        private String country;
        private double lat;
        private double lon;

        @JsonProperty("tz_id")
        private String tzId;

        @JsonProperty("localtime_epoch")
        private String localtimeEpoch;
        private String localtime;
    }

    @Setter
    @Getter
    public static class CurrentWeatherDto {
        private double tempC;
        private double tempF;

        @JsonProperty("is_day")
        private boolean isDay;

        private Condition condition;

        @JsonProperty("wind_kph")
        private double windKph;

        private int humidity;

        @JsonProperty("feelslike_c")
        private double feelslikeC;

        @JsonProperty("feelslike_f")
        private double feelslikeF;

        @JsonProperty("pressure_mb")
        private double pressureMb;

        @JsonProperty("precip_mm")
        private double precipMm;

        @JsonProperty("windchill_c")
        private double windchillC;

        @JsonProperty("dewpoint_c")
        private double dewpointC;

        @JsonProperty("vis_km")
        private double visKm;

        private int uv;

        @JsonProperty("gust_kph")
        private double gustKph;
    }

    @Setter
    @Getter
    public static class ForecastDto {
        private List<ForecastDayDto> forecastday;

        @Setter
        @Getter
        public static class ForecastDayDto {
            private String date;

            @JsonProperty("date_epoch")
            private long dateEpoch;

            private DayDto day;

            private AstroDto astro;

            @JsonProperty("hour")
            private List<HourlyWeatherDto> hourlyWeather;

            @Setter
            @Getter
            public static class AstroDto {
                private String sunrise;
                private String sunset;
                private String moonrise;
                private String moonset;
                private String moonPhase;

                @JsonProperty("moon_illumination")
                private int moonIllumination;

                @JsonProperty("is_moon_up")
                private boolean isMoonUp;

                @JsonProperty("is_sun_up")
                private boolean isSunUp;
            }

            @Setter
            @Getter
            public static class DayDto {
                @JsonProperty("maxtemp_c")
                private double maxtempC;

                @JsonProperty("mintemp_c")
                private double mintempC;

                @JsonProperty("avgtemp_c")
                private double avgtempC;

                @JsonProperty("maxwind_kph")
                private double maxwindKph;

                @JsonProperty("totalprecip_mm")
                private double totalprecipMm;

                private String conditionText;
                private String conditionIcon;

                private double uv;
            }
        }

        @Setter
        @Getter
        public static class HourlyWeatherDto {
            @JsonProperty("time")
            private String time;

            @JsonProperty("temp_c")
            private double tempC;

            @JsonProperty("humidity")
            private int humidity;

            @JsonProperty("precip_mm")
            private double precipMm;

            @JsonProperty("windspeed_kph")
            private double windspeedKph;

            @JsonProperty("condition")
            private Condition condition;

        }
    }
}
