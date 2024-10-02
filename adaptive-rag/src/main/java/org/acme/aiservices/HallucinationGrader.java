package org.acme.aiservices;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.input.structured.StructuredPromptProcessor;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@ApplicationScoped
public class HallucinationGrader {


    @StructuredPrompt("""
        Set of facts: {{documents}} LLM generation: {{generation}}
    """)
    public static class Arguments {
        List<String> documents;
        String generation;

        public Arguments(List<String> documents, String generation) {
            this.documents = documents;
            this.generation = generation;
        }
    }

    @Singleton
    @RegisterAiService
    public interface Service {
        @SystemMessage("""
                You are a grader assessing whether an LLM generation is grounded in / supported by a set of retrieved facts.
                Give a binary score 'yes' or 'no'. 'Yes' means that the answer is grounded in / supported by the set of facts.
                """)
        Score invoke(String userMessage);
    }

    @Inject
    Service service;

    public Score detectHallucination(Arguments args) {
        Prompt prompt = StructuredPromptProcessor.toPrompt(args);
        return service.invoke(prompt.text());
    }

}
