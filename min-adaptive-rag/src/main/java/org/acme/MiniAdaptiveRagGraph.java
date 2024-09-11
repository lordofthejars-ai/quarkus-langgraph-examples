package org.acme;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;

import org.bsc.langgraph4j.state.AgentState;

import org.jboss.logging.Logger;

import static java.util.Collections.emptyList;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.utils.CollectionsUtils.listOf;
import static org.bsc.langgraph4j.utils.CollectionsUtils.mapOf;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;

@ApplicationScoped
public class MiniAdaptiveRagGraph {

    @Inject
    Logger logger;

    @Inject
    WebSearchTool webSearchTool;

    @Inject
    EmbeddingStoreRetrieval embeddingStoreRetrieval;

    @Inject
    QuestionRouter questionRouter;

    @Inject
    GenerateAnswer generateAnswer;

    public static class State extends AgentState {

        public State(Map<String, Object> initData) {
            super(initData);
        }

        public String question() {
            Optional<String> result = value("question");
            return result.orElseThrow( () -> new IllegalStateException( "question is not set!" ) );
        }
        public Optional<String> generation() {
            return value("generation");

        }
        public List<String> documents() {
            return this.<List<String>>value("documents").orElse(emptyList());
        }

    }

    @Produces
    public CompiledGraph<State> buildGraph() throws Exception {
        return new StateGraph<>(State::new)
        // Define the nodes
            .addConditionalEdges(START,
                edge_async(this::routeQuestion),
                mapOf(
                    "web_search", "web_search",
                    "embedding", "retrieve_rag"
                ))

            .addNode("web_search", node_async(this::webSearch) )
            .addNode("retrieve_rag", node_async(this::retrieve) )
            .addNode("generate", node_async(this::generateResponse) )

            .addEdge("web_search", "generate")
            .addEdge("retrieve_rag", "generate")

            .addEdge("generate", END)
            .compile();
    }

    private String routeQuestion(State state) {

        logger.info("-> Route Question Call");

        String question = state.question();

        final String route = questionRouter.routeToNode(question);
        logger.infof("    -> Routing to %s", route);

        return route;

    }

    private Map<String,Object> retrieve( State state ) {
        logger.info("-> Retrieving documents from Chroma");

        String question = state.question();

        final EmbeddingSearchResult<TextSegment> relevant = this.embeddingStoreRetrieval.search(question);

        List<String> documents = relevant.matches().stream()
            .map( m -> m.embedded().text() )
            .collect(Collectors.toList());

        logger.infof("Retrieved content %s.", documents);

        return mapOf( "documents", documents , "question", question );
    }

    private Map<String, Object> webSearch(State state) {

        logger.info("-> Web Search call");

        final List<Content> contents = webSearchTool.search(state.question());

        var webResult = contents.stream()
            .map( content -> content.textSegment().text() )
            .collect(Collectors.joining("\n"));

        logger.infof("Retrieved content %s.", webResult);

        return mapOf( "documents", listOf( webResult ) );

    }

    private Map<String,Object> generateResponse( State state ) {
        logger.info("-> Generate a response with te given data");

        String question = state.question();
        List<String> documents = state.documents();

        final String generation = generateAnswer.generateAnswer(question, documents);

        return mapOf("generation", generation);
    }
}
