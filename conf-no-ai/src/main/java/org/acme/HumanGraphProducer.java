package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Singleton
public class HumanGraphProducer {

    public static class State extends AgentState {

        public State(Map<String, Object> initData) {
            super(initData);
        }

        public String query() {
            return value("query", "No query provided");
        }

        public int i() {
            return value("i", 0);
        }

        public String humanChar() {
            return value("humanChar", "");
        }

        public Optional<String> msg1() {
            return value("msg1");
        }

        public Optional<String> msg2() {
            return value("msg2");
        }
        
    }

    private Map<String, Object> toUpperCase(State state) {
        String query = state.query();
        return Map.of("msg1", query.toUpperCase());
    }

    private Map<String, Object> surround(State state, String charz) {
        String msg = state.msg1().orElseThrow();
        return Map.of("msg2", charz + msg + charz);
    }

    private String conditionToSurround(State state) {
        int number = state.i();
        return number % 2 == 0 ? "star" : "plus";
    }

    @Produces
    @Singleton
    BaseCheckpointSaver memorySaver() {
        return new MemorySaver();
    }

    @Produces
    @Named("human-conditional-graph")
    @Singleton
    CompiledGraph<State> createHumanGraph(BaseCheckpointSaver checkpointSaver) throws GraphStateException {
        CompileConfig.Builder config = new CompileConfig.Builder();
        config.checkpointSaver(checkpointSaver);

        config.interruptAfter("node_2", "node_3");

        return new StateGraph<>(State::new)
            .addEdge(StateGraph.START,"node_1")
            .addNode("node_1", node_async(this::toUpperCase))
            .addNode("node_2", node_async(state -> this.surround(state,"*")))
            .addNode("node_3", node_async(state -> this.surround(state,"+")))
            .addConditionalEdges("node_1", edge_async(this::conditionToSurround),
                Map.of("star", "node_2", "plus", "node_3"))
            .addNode("node_4", node_async(state -> this.surround(state, state.humanChar())))
            .addEdge("node_2", "node_4")
            .addEdge("node_3", "node_4")
            .addEdge("node_4", StateGraph.END)
            .compile(config.build());

    }

}
