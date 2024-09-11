package org.acme;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;

import org.bsc.langgraph4j.state.AgentState;


import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@ApplicationScoped
public class GraphProducer {

    @Inject
    RedisSaver checkpointSaver;

    public static class State extends AgentState {

        public State(Map<String, Object> initData) {
            super(initData);
        }

        public Optional<String> msg() {
            return value("msg");
        }

    }


    public CompiledGraph<State> buildSingleGraphTimeTravel() throws Exception {

        CompileConfig.Builder config = new CompileConfig.Builder();

        if( checkpointSaver != null ) {
            config.checkpointSaver(checkpointSaver);
        }

        config.interruptAfter("node_2");

        return new StateGraph<>(State::new)
            .addEdge(START,"node_1")
            .addNode("node_1", node_async(state -> {
                //throw new RuntimeException();
                System.out.println("Function 1");

                return Map.of("msg", "Function 1");
            }))
            .addNode("node_2", node_async(state -> {
                System.out.println("Function 2");
                return Map.of("msg", "Function 2");
            }))
            .addNode("node_3", node_async(state -> {
                System.out.println("Function 3");
                return Map.of("msg", "Function 3");
            }))
            .addEdge("node_1", "node_2")
            .addEdge("node_2", "node_3")
            .addEdge("node_3", END)
            .compile(config.build())
            ;
    }



}
