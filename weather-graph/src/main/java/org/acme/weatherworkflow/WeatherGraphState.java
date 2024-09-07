package org.acme.weatherworkflow;

import java.util.Map;
import java.util.Optional;
import org.acme.weather.DailyWeatherData;
import org.bsc.langgraph4j.state.AgentState;

public class WeatherGraphState extends AgentState {

    public WeatherGraphState(Map<String, Object> initData) {
        super(initData);
    }

    public String query() {
        return value("query", "No query provided");
    }

    public Optional<String> city() {
        return value("city");
    }

    public Optional<DailyWeatherData> daily() {
        return value("daily");
    }

    public Optional<String> forecast() {
        return value("forecast");
    }

    public boolean isCorrectCity() {
        final Optional<String> optionalCity = city();
        String city = optionalCity.orElse(CityExtractorAgent.NO_CITY);

        return ! CityExtractorAgent.NO_CITY.equals(city);
    }
}
