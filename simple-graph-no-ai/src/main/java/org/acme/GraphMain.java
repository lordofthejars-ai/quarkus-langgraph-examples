package org.acme;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.util.Optional;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.utils.CollectionsUtils;

@QuarkusMain
public class GraphMain implements QuarkusApplication {


    @Override
    public int run(String... args) throws Exception {


        final CompiledGraph<GraphProducer.State> graph = GraphProducer.buildSingleGraph();

        var runnableConfig =  RunnableConfig.builder()
            .threadId("conversation-num-1" )
            .build();

        final Optional<GraphProducer.State> invoke = graph
            .invoke(CollectionsUtils.mapOf(), runnableConfig);

        System.out.println(invoke.get());
        System.out.println(GraphProducer.checkpointSaver.list(runnableConfig));
        /**final Optional<GraphProducer.AppenderState> invoke1 = GraphProducer.buildSingleGraphAppender().invoke(CollectionsUtils.mapOf());
        System.out.println(invoke1.get().msg());

        final Optional<GraphProducer.AppenderState> invoke2 = GraphProducer.buildConditionalGraphAppender().invoke(CollectionsUtils.mapOf());
        System.out.println(invoke2.get().msg());**/



        return 0;
    }


}
