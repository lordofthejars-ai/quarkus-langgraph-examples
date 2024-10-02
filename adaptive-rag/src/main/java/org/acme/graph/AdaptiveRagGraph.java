package org.acme.graph;

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
import org.acme.aiservices.AnswerGrader;
import org.acme.aiservices.GenerateAnswer;
import org.acme.aiservices.HallucinationGrader;
import org.acme.aiservices.QuestionRewriter;
import org.acme.aiservices.QuestionRouter;
import org.acme.aiservices.RetrievalGrader;
import org.acme.aiservices.Score;
import org.acme.aitools.EmbeddingStoreRetrieval;
import org.acme.aitools.WebSearchTool;
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
public class AdaptiveRagGraph {

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

    @Inject
    RetrievalGrader retrievalGrader;

    @Inject
    QuestionRewriter questionRewriter;

    @Inject
    HallucinationGrader hallucinationGrader;

    @Inject
    AnswerGrader answerGrader;

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
            .addNode("grade_documents",  node_async(this::gradeDocuments) )
            .addNode("retrieve_rag", node_async(this::retrieve) )
            .addNode("generate", node_async(this::generateResponse))
            .addNode("transform_query", node_async(this::transformQuery))

            .addEdge("web_search", "generate")
            .addEdge("retrieve_rag", "grade_documents")
            .addConditionalEdges(
                "grade_documents",
                edge_async(this::areDocumentsRelevant),
                mapOf(
                    "transform_query","transform_query",
                    "generate", "generate"
                ))
            .addEdge("transform_query", "retrieve_rag")
            .addConditionalEdges(
                "generate",
                edge_async(this::isAnswerGroundedWithoutHallucinations),
                mapOf(
                    "not supported", "generate",
                    "useful", END,
                    "not useful", "transform_query"
                ))
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

    private Map<String,Object> gradeDocuments( State state ) {
        logger.info("-> Check document relevance to Question");

        String question = state.question();

        List<String> documents = state.documents();

        List<String> filteredDocs =  documents.stream()
            .filter( d -> {
                var score = retrievalGrader.gradeDocument( new RetrievalGrader.Arguments(question, d ));

                if( score.binaryScore ) {
                    logger.info("    -> Grade: Document relevant");
                }
                else {
                    logger.info("    -> Grade: Document not relevant");
                }
                return score.binaryScore;
            })
            .collect(Collectors.toList());

        return mapOf( "documents", filteredDocs);
    }

    private Map<String,Object> transformQuery(State state) {
        logger.info("-> Transform Query");

        String question = state.question();
        return mapOf( "question", questionRewriter.rewriteQuestion(question));
    }

    private String areDocumentsRelevant(State state) {
        logger.info("-> Asses graded documents");
        List<String> documents = state.documents();

        if(documents.isEmpty()) {
            logger.info("    -> Decision: All documents are not relevant to question, transform query");
            return "transform_query";
        }
        logger.info( "-> Decision: Generate" );
        return "generate";
    }

    private String isAnswerGroundedWithoutHallucinations( State state ) {
        logger.info("-> Check Hallucinations");

        String question = state.question();
        List<String> documents = state.documents();
        String generation = state.generation()
            .orElseThrow( () -> new IllegalStateException( "generation is not set!" ) );

        Score score = hallucinationGrader
            .detectHallucination(new HallucinationGrader.Arguments(documents, generation));

        if(score.binaryScore) {
            logger.info( "-> Decision: Generation is grounded in documents");
            logger.info("    -> Grade generation vs question");

            Score score2 =  answerGrader.gradeAnswer( new AnswerGrader.Arguments(question, generation) );

            if( score2.binaryScore) {
                logger.info("        -> Decision: Generation addresses question");
                return "useful";
            }

            logger.info("    -> Decision: Generation does not address question");
            return "not useful";
        }

        logger.info( "-> Decision: Generation is not grounded in documents, re-try" );
        return "not supported";
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
