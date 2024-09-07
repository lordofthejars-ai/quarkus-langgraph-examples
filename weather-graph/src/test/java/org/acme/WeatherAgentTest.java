package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.weather.Daily;
import org.acme.weather.WeatherForecast;
import org.acme.weatherworkflow.WeatherAgent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class WeatherAgentTest {

    @Inject
    WeatherAgent weatherAgent;

    @Test
    public void queryWeatherForecast() {

        final WeatherForecast berlinWeather = weatherAgent.retrieve("Berlin");

        assertThat(berlinWeather).isNotNull();

        final Daily daily = berlinWeather.daily();
        assertThat(daily).isNotNull();

        assertThat(daily.weather_code()).hasSize(1);
        assertThat(daily.temperature_2m_max()).hasSize(1);
        assertThat(daily.precipitation_sum()).hasSize(1);
        assertThat(daily.temperature_2m_min()).hasSize(1);
        assertThat(daily.precipitation_sum()).hasSize(1);

        System.out.println(daily);

    }

}
