package org.acme.aiservices;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@ApplicationScoped
public class GenerateAnswer {

    @Singleton
    @RegisterAiService
    public interface Service {

        @UserMessage(""" 
            You are an assistant for question-answering tasks. 
            Use the following pieces of retrieved context to answer the question. 
            If you don't know the answer, just say that you don't know. 
            Use three sentences maximum and keep the answer concise.
            
            Question: {{question}} 
            Context: {{context}}
            """)
        String generate(@V("question") String question, @V("context") List<String> context );
    }

    @Inject
    Service service;

    public String generateAnswer(String question, List<String> context) {
        return service.generate(question, context);
    }
}
