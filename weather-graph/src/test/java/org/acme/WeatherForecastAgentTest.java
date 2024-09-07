package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.weather.Daily;
import org.acme.weatherworkflow.WeatherForecastAgent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class WeatherForecastAgentTest {

    @Inject
    WeatherForecastAgent weatherForecastAgent;

    @Test
    public void generateForecast() {

        Daily daily = DailyMotherObject.createDaily();

        final String chat = weatherForecastAgent.chat("What is the weather in Berlin?", daily.getFirstDay().toJson());
        assertThat(chat).contains("Berlin");
    }

}
