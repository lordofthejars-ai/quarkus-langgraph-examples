package org.acme.weatherworkflow;


import dev.langchain4j.rag.content.Content;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.acme.geo.GeoCodingService;
import org.acme.geo.GeoResults;

import org.acme.weather.WeatherForecast;
import org.acme.weather.WeatherForecastService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class WeatherAgent {

    private static final String[] PARAMETERS = new String[] {
            "temperature_2m_max",
            "temperature_2m_min",
            "precipitation_sum",
            "wind_speed_10m_max",
            "weather_code"
    };

    @RestClient
    GeoCodingService geoCodingService;

    @RestClient
    WeatherForecastService weatherForecastService;

    public WeatherForecast retrieve(String city) {

        List<Content> results = new ArrayList<>();

        GeoResults search = geoCodingService.search(city, 1);
        double latitude = search.getFirst().latitude();
        double longitude = search.getFirst().longitude();

        return weatherForecastService.forecast(latitude, longitude,
                1, PARAMETERS);

    }
}
