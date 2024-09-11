package org.acme.weatherworkflow;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import org.acme.weather.DailyWeatherData;
import org.acme.weather.WeatherForecast;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.serializer.Serializer;
import org.bsc.langgraph4j.serializer.StateSerializer;
import org.jetbrains.annotations.NotNull;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@ApplicationScoped
public class WeatherGraphProducer {

    @Inject
    CityExtractorAgent cityExtractorAgent;

    @Inject
    WeatherAgent weatherAgent;

    @Inject
    WeatherForecastAgent weatherForecastAgent;

    @Startup
    public void registerSerializers() {
        StateSerializer.register(DailyWeatherData.class, new Serializer<DailyWeatherData>() {
            @Override
            public void write(DailyWeatherData object, ObjectOutput out) throws IOException {
                out.writeDouble(object.temperature_2m_max());
                out.writeDouble(object.temperature_2m_min());
                out.writeDouble(object.precipitation_sum());
                out.writeDouble(object.wind_speed_10m_max());
                out.writeInt(object.weather_code());
            }

            @Override
            public DailyWeatherData read(ObjectInput in) throws IOException {
                return new DailyWeatherData(in.readDouble(), in.readDouble(),
                    in.readDouble(), in.readDouble(), in.readInt());
            }
        });
    }

    @Produces
    public CompiledGraph<WeatherGraphState> buildGraph() throws Exception {
        return buildGraph(getCityName(), getDailyWeather(), getForecast());
    }

    private @NotNull NodeAction<WeatherGraphState> getForecast() {
        return weatherGraphState -> {
            final DailyWeatherData daily = weatherGraphState.daily().get();
            final String chat = weatherForecastAgent.chat(weatherGraphState.query(), daily.toJson());
            return Map.of("forecast", chat);
        };
    }

    private @NotNull NodeAction<WeatherGraphState> getDailyWeather() {
        return weatherGraphState -> {
            String city = weatherGraphState.city().get();
            final WeatherForecast weatherForecast = weatherAgent.retrieve(city);
            return Map.of("daily", weatherForecast.daily().getFirstDay());
        };
    }

    private @NotNull NodeAction<WeatherGraphState> getCityName() {
        return weatherGraphState -> {
            String query = weatherGraphState.query();
            String city = cityExtractorAgent.extractCity(query);
            return Map.of("city", city);
        };
    }

    protected CompiledGraph<WeatherGraphState> buildGraph(
        NodeAction<WeatherGraphState> cityExtractor,
        NodeAction<WeatherGraphState> weatherAgent,
        NodeAction<WeatherGraphState> weatherForecast
        ) throws Exception {

        CompileConfig.Builder config = new CompileConfig.Builder();

        return new StateGraph<>(WeatherGraphState::new)
            .addNode("city_extractor", node_async(cityExtractor))
            .addNode("weather_agent", node_async(weatherAgent))
            .addNode("weather_forecast", node_async(weatherForecast))

            .addEdge(START,"city_extractor")
            .addConditionalEdges("city_extractor",
                edge_async(state -> state.isCorrectCity() ? "continue" : "end"),
                Map.of("continue", "weather_agent", "end", END))
            .addEdge("weather_agent", "weather_forecast")
            .addEdge("weather_forecast", END)
            .compile(config.build())
            ;
    }

}
