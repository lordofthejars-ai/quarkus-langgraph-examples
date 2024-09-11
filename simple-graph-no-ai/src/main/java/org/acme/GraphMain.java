package org.acme;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.util.Map;
import java.util.Optional;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.state.StateSnapshot;
import org.bsc.langgraph4j.utils.CollectionsUtils;

@QuarkusMain
public class GraphMain implements QuarkusApplication {


    @Override
    public int run(String... args) throws Exception {

        final CompiledGraph<GraphProducer.State> graph = GraphProducer.buildSingleGraphTimeTravel();

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

        var updateConfig = graph.updateState(state.getConfig(), CollectionsUtils.mapOf("msg", "I am a human message"), null);
        System.out.println(updateConfig);

        final Optional<GraphProducer.State> invoke2 = graph.invoke(null, updateConfig);

        System.out.println(invoke2.get());

        /**final CompiledGraph<GraphProducer.State> graph = GraphProducer.buildSingleGraphTimeTravel();

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

        System.out.println(invoke2.get());**/

        /**final CompiledGraph<GraphProducer.State> graph = GraphProducer.buildSingleGraph();

        var runnableConfig =  RunnableConfig.builder()
            .threadId("conversation-num-1" )
            .build();

        final Optional<GraphProducer.State> invoke = graph
            .invoke(CollectionsUtils.mapOf(), runnableConfig);

        System.out.println(invoke.get());
        System.out.println(GraphProducer.checkpointSaver.list(runnableConfig));**/

        /**final Optional<GraphProducer.AppenderState> invoke1 = GraphProducer.buildSingleGraphAppender().invoke(CollectionsUtils.mapOf());
        System.out.println(invoke1.get().msg());

        final Optional<GraphProducer.AppenderState> invoke2 = GraphProducer.buildConditionalGraphAppender().invoke(CollectionsUtils.mapOf());
        System.out.println(invoke2.get().msg());**/



        return 0;
    }


}
