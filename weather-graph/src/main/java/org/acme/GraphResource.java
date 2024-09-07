package org.acme;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;
import org.acme.weatherworkflow.WeatherGraphState;

import org.bsc.langgraph4j.CompiledGraph;


@Path("/weather")
public class GraphResource {

    @Inject
    CompiledGraph<WeatherGraphState> graph;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getWeather() throws Exception {

        final Optional<WeatherGraphState> state = graph.invoke(Map.of("query", "What is the weather in Berlin?"));
        return state.get().forecast().get();

    }


}
