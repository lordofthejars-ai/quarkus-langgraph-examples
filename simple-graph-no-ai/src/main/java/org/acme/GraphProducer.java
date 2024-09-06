package org.acme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.AppenderChannel;
import org.bsc.langgraph4j.state.Channel;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class GraphProducer {

    private static Random random = new Random();

    static BaseCheckpointSaver checkpointSaver = new MemorySaver();

    public static class State extends AgentState {

        public State(Map<String, Object> initData) {
            super(initData);
        }

        public Optional<String> msg() {
            return value("msg");
        }

    }

    public static class AppenderState extends AgentState {

        static Map<String, Channel<?>> SCHEMA = Map.of(
            "msg", AppenderChannel.<String>of(ArrayList::new)
        );

        public AppenderState(Map<String, Object> initData) {
            super(initData);
        }

        List<String> msg() {
            return this.<List<String>>value("msg").orElseGet(ArrayList::new);
        }

    }

    public static CompiledGraph<AppenderState> buildConditionalGraphAppender() throws Exception {

        return new StateGraph<>(AppenderState.SCHEMA, AppenderState::new)
            .addEdge(START,"node_1")
            .addNode("node_1", node_async(state -> {
                System.out.println(state.getClass());
                System.out.println("Function 1");
                return Map.of("msg", "Function 1");
            }))
            .addNode("node_2", node_async(state -> {
                System.out.println("Function 2");
                return Map.of("msg", "Function 2");
            }))
            .addConditionalEdges("node_1", edge_async(t -> {
                int number = random.nextInt(10);
                System.out.println(number);
                return number % 2 == 0 ? "continue" : "end";
            }), Map.of("continue", "node_2", "end", END))
            //.addEdge("node_1", "node_2")
            .addEdge("node_2", END)
            .compile()
            ;
    }

    public static CompiledGraph<AppenderState> buildSingleGraphAppender() throws Exception {

        return new StateGraph<>(AppenderState.SCHEMA, AppenderState::new)
            .addEdge(START,"node_1")
            .addNode("node_1", node_async(state -> {
                System.out.println(state.getClass());
                System.out.println("Function 1");
                return Map.of("msg", "Function 1");
            }))
            .addNode("node_2", node_async(state -> {
                System.out.println("Function 2");
                return Map.of("msg", "Function 2");
            }))
            .addEdge("node_1", "node_2")
            .addEdge("node_2", END)
            .compile()
            ;
    }

    public static CompiledGraph<State> buildSingleGraph() throws Exception {

        CompileConfig.Builder config = new CompileConfig.Builder();

        if( checkpointSaver != null ) {
            config.checkpointSaver(checkpointSaver);
        }

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
            .addEdge("node_1", "node_2")
            .addEdge("node_2", END)
            .compile(config.build())
        ;
    }

}
