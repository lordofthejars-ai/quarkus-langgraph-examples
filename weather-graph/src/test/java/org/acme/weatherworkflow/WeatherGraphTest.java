package org.acme.weatherworkflow;

import java.util.Map;
import java.util.Optional;
import org.acme.DailyMotherObject;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WeatherGraphTest {

    private WeatherGraphProducer weatherGraphProducer = new WeatherGraphProducer();

    @Test
    public void normalFlow() throws Exception {
        final CompiledGraph<WeatherGraphState> weatherGraphStateCompiledGraph =
            weatherGraphProducer.buildGraph(cityExtractor(), weatherData(), forecast());

        final Optional<WeatherGraphState> state = weatherGraphStateCompiledGraph.invoke(Map.of());

        assertThat(state).isPresent();

        final WeatherGraphState weatherGraphState = state.get();
        assertThat(weatherGraphState.isCorrectCity()).isTrue();
        assertThat(weatherGraphState.city()).isPresent().contains("Barcelona");
        assertThat(weatherGraphState.daily()).isPresent();
        assertThat(weatherGraphState.forecast()).isPresent().contains("Good Weather");
    }

    @Test
    public void exceptionFlow() throws Exception {
        final CompiledGraph<WeatherGraphState> weatherGraphStateCompiledGraph =
            weatherGraphProducer.buildGraph(cityExtractorNoCity(), weatherData(), forecast());

        final Optional<WeatherGraphState> state = weatherGraphStateCompiledGraph.invoke(Map.of());

        assertThat(state).isPresent();

        final WeatherGraphState weatherGraphState = state.get();
        assertThat(weatherGraphState.isCorrectCity()).isFalse();
        assertThat(weatherGraphState.daily()).isNotPresent();
        assertThat(weatherGraphState.forecast()).isNotPresent();
    }


    private NodeAction<WeatherGraphState> cityExtractorNoCity() {
        return weatherGraphState -> Map.of("city", CityExtractorAgent.NO_CITY);
    }

    private NodeAction<WeatherGraphState> cityExtractor() {
        return weatherGraphState -> Map.of("city", "Barcelona");
    }

    private NodeAction<WeatherGraphState> weatherData() {
        return weatherGraphState -> Map.of("daily", DailyMotherObject.createDaily());
    }

    private NodeAction<WeatherGraphState> forecast() {
        return weatherGraphState -> Map.of("forecast", "Good Weather");
    }

}
