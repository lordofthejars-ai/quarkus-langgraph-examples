package org.acme.graph;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.acme.aiservices.CategorizeEmail;
import org.acme.aiservices.DraftEmailAnalysis;
import org.acme.aiservices.ExtractQuestionsEmail;
import org.acme.aiservices.ResearchRouter;
import org.acme.aiservices.RewriteDraftEmail;
import org.acme.aiservices.RewriteRouter;
import org.acme.aiservices.WriteDraftEmail;
import org.acme.aitools.WebSearchTool;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@ApplicationScoped
public class EmailReplayGraphProducer {

    @Inject
    CategorizeEmail categorizeEmail;

    @Inject
    ResearchRouter researchRouter;

    @Inject
    WriteDraftEmail writeDraftEmail;

    @Inject
    WebSearchTool webSearchTool;

    @Inject
    ExtractQuestionsEmail extractQuestionsEmail;

    @Inject
    RewriteRouter rewriteRouter;

    @Inject
    DraftEmailAnalysis draftEmailAnalysis;

    @Inject
    RewriteDraftEmail rewriteDraftEmail;

    public static class State extends AgentState {

        public State(Map<String, Object> initData) {
            super(initData);
        }

        public String initialEmail() {
            Optional<String> result = value("initial_email");
            return result.orElseThrow( () -> new IllegalStateException( "email is not set!" ) );
        }

        public String emailCategory() {
            Optional<String> result = value("email_category");
            return result.orElse("off_topic");
        }

        public String draftEmail() {
            Optional<String> result = value("draft_email");
            return result.orElseThrow(() -> new IllegalStateException( "draft email is not set!" ));
        }

        public String finalEmail() {
            Optional<String> result = value("final_email");
            return result.orElseThrow(() -> new IllegalStateException( "final email is not set!" ));
        }

        public List<String> researchInfo() {
            Optional<List<String>> result = value("research_info");
            return result.orElse(Collections.emptyList());
        }

    }


    @Produces
    public CompiledGraph<State> buildGraph() throws Exception {
        return new StateGraph<>(State::new)

            .addEdge(START,"email_category")
            .addNode("email_category", node_async(this::categorizeEmail))
            .addConditionalEdges("email_category", edge_async(this::routeEmail),
                Map.of("research_info", "research_info", "draft_email","draft_email"))
            .addNode("draft_email", node_async(this::draftAResponse))
            .addNode("research_info", node_async(this::researchInfo))
            .addEdge("research_info", "draft_email")
            .addConditionalEdges("draft_email", edge_async(this::rewriteRoute),
                Map.of("rewrite", "rewrite", "no_rewrite", "no_rewrite"))
            .addNode("no_rewrite", node_async(this::endProcess))
            .addEdge("no_rewrite", END)
            .addNode("rewrite", node_async(this::analyzeAndRewriteDraft))
            .addEdge("rewrite", "no_rewrite")
            .compile();
    }

    private Map<String,Object> categorizeEmail(State state) {
        String emailCategory = this.categorizeEmail.categorize(state.initialEmail());
        return Map.of("email_category", emailCategory);
    }

    private String routeEmail(State state) {
        return this.researchRouter.route(state.initialEmail(), state.emailCategory());
    }

    private Map<String, Object> draftAResponse(State state) {
        String email = this.writeDraftEmail.writeDraftEmail(state.initialEmail(), state.emailCategory(), state.researchInfo());
        return Map.of("draft_email", email);
    }

    private Map<String,Object> researchInfo(State state) {
        final JsonNode questions = this.extractQuestionsEmail.findQuestions(state.initialEmail(), state.emailCategory());
        final Iterator<JsonNode> questionsIterator = questions.get("questions").elements();

        final List<String> listOfQuestions = Stream.generate(() -> null)
            .takeWhile(x -> questionsIterator.hasNext())
            .map(n -> questionsIterator.next())
            .map(JsonNode::asText)
            .map(q -> this.webSearchTool.search(q))
            .toList();

        return Map.of("research_info", listOfQuestions);
    }

    private String rewriteRoute(State state) {
        return this.rewriteRouter.route(state.initialEmail(), state.emailCategory(), state.draftEmail());
    }

    private Map<String, Object> endProcess(State state) {
        return Map.of("final_email", state.draftEmail());
    }

    private Map<String, Object> analyzeAndRewriteDraft(State state) {

        String researchInfo = String.join(" ", state.researchInfo());

        String hints = this.draftEmailAnalysis.analyze(state.initialEmail(), state.emailCategory(), researchInfo,
            state.draftEmail());

        String newEmail = this.rewriteDraftEmail.improveDraftEmailWithAnalysis(state.initialEmail(), state.emailCategory(), researchInfo,
            state.draftEmail(), hints);

        return Map.of("draft_email", newEmail);

    }

}
