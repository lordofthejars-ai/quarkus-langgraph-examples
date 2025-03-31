package org.acme;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.state.StateSnapshot;
import org.bsc.langgraph4j.utils.CollectionsUtils;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/hello")
public class GraphResource {

    @Inject
    @Named("conditional-graph")
    CompiledGraph<ConditionalGraphProducer.State> conditionalCompiledGraph;

    @Inject
    @Named("human-conditional-graph")
    CompiledGraph<HumanGraphProducer.State> humanConditionalGraph;

    @POST
    @Path("/human/{userId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String executeHumanGraph(String content, @RestQuery int i, @RestPath String userId) throws Exception {

        var runnableConfig =  RunnableConfig.builder()
            .threadId(userId)
            .build();

        final Optional<HumanGraphProducer.State> optionalState =
            humanConditionalGraph.invoke(Map.of("query", content, "i", i), runnableConfig);
        final HumanGraphProducer.State state = optionalState.get();

        System.out.println(state.msg2());
        System.out.println(state.humanChar());

        System.out.println("**************** State Snapshot ***************+");
        final StateSnapshot<HumanGraphProducer.State> stateSnapshot = humanConditionalGraph.getState(runnableConfig);
        System.out.println(stateSnapshot.getState());
        System.out.println(stateSnapshot.getNext());
        System.out.println(stateSnapshot.getConfig());

        return optionalState.get().msg2().orElseThrow();
    }

    @POST
    @Path("/human/continue/{userId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String continueHumanGraph(String content, @RestPath String userId) throws Exception {

        var runnableConfig =  RunnableConfig.builder()
            .threadId(userId)
            .build();

        var updateConfig = humanConditionalGraph.updateState(runnableConfig,
            CollectionsUtils.mapOf("humanChar", "-"), null);

        System.out.println("**************** After Manual ***************+");

        System.out.println(updateConfig);

        final Optional<HumanGraphProducer.State> optionalState2 =
            humanConditionalGraph.invoke(null, updateConfig);

        return optionalState2.get().msg2().orElseThrow();

    }

    @POST
    @Path("/conditional")
    @Produces(MediaType.TEXT_PLAIN)
    public String conditionalGraph(String content, @RestQuery int i) throws Exception {

        final Optional<ConditionalGraphProducer.State> optionalState =
            conditionalCompiledGraph.invoke(Map.of("query", content, "i", i));

        final ConditionalGraphProducer.State state = optionalState.get();

        System.out.println(state.msg1());
        System.out.println(state.msg2());

        return state.msg2().orElseThrow();
    }
}
