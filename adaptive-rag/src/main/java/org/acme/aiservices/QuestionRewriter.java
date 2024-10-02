package org.acme.aiservices;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bsc.langgraph4j.utils.CollectionsUtils;

@ApplicationScoped
public class QuestionRewriter {

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage(
            """
                You a question re-writer that converts an input question to a better version that is optimized.
                for vector store retrieval. Look at the input and try to reason about the underlying semantic intent / meaning.
            """)
        String rewrite(String question);
    }

    @Inject
    Service service;

    public String rewriteQuestion(String question) {

        final PromptTemplate template = PromptTemplate.from("""
            Here is the initial question: {{question}} Formulate an improved question.
        """);

        final Prompt prompt = template.apply( CollectionsUtils.mapOf( "question", question ) );
        return service.rewrite( prompt.text() );
    }

}
