package org.acme;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import java.util.Optional;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.state.StateSnapshot;
import org.bsc.langgraph4j.utils.CollectionsUtils;

@QuarkusMain
public class GraphMain implements QuarkusApplication {

    @Inject
    GraphProducer graphProducer;

    @Override
    public int run(String... args) throws Exception {

        final CompiledGraph<GraphProducer.State> graph = graphProducer.buildSingleGraphTimeTravel();

        var runnableConfig =  RunnableConfig.builder()
            .threadId("conversation-num-1" )
            .build();

        final Optional<GraphProducer.State> invoke = graph
            .invoke(CollectionsUtils.mapOf(), runnableConfig);

        final StateSnapshot<GraphProducer.State> state = graph.getState(runnableConfig);
        System.out.println(state.getState());
        System.out.println(state.getNext());

        System.out.println("*****************");

        System.out.println(state.getConfig());

        final Optional<GraphProducer.State> invoke2 = graph.invoke(null, state.getConfig());

        System.out.println(invoke2.get());

        return 0;
    }


}
