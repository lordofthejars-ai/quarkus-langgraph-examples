package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;
import org.acme.graph.AdaptiveRagGraph;
import org.bsc.langgraph4j.CompiledGraph;

@Path("/hello")
public class RagResource {

    @Inject
    CompiledGraph<AdaptiveRagGraph.State> graph;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws Exception {

        /**final Map<String, Object> inputs = Map.of("question", "what is the price of a new Flux capacitor for DeLorean car");

        final Optional<MiniAdaptiveRagGraph.State> result = graph.invoke(inputs);
        System.out.println(result.get().generation().get());**/

        final Map<String, Object> inputs2 = Map.of("question", "Is there any flight between Barcelona and Brussels?");

        final Optional<AdaptiveRagGraph.State> result2 = graph.invoke(inputs2);
        System.out.println(result2.get().generation().get());

        return "Hello from Quarkus REST";
    }
}
