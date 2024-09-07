package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.weatherworkflow.CityExtractorAgent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class CityExtractorAgentTest {

    @Inject
    CityExtractorAgent cityExtractorAgent;

    @ParameterizedTest
    @CsvSource({
        "What is the weather in Berlin?,              Berlin",
        "Is it raining in Tokyo?,                     Tokyo",
        "Can you send me the forecast for Barcelona?, Barcelona",
        "How Are you?,                                no_response"
    })
    public void extractCity(String msg, String city) {
        assertThat(cityExtractorAgent.extractCity(msg)).isEqualTo(city);
    }

}
