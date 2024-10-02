package org.acme.aiservices;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.structured.StructuredPrompt;

import dev.langchain4j.model.input.structured.StructuredPromptProcessor;
import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@ApplicationScoped
public class AnswerGrader {

    @StructuredPrompt("""
        User question: {{question}} LLM generation: {{generation}}
    """)
    public static class Arguments {
        String question;
        String generation;

        public Arguments(String question, String generation) {
            this.question = question;
            this.generation = generation;
        }
    }

    @Singleton
    @RegisterAiService
    public interface Service {
        @SystemMessage("""
            You are a grader assessing whether an answer addresses and/or resolves a question.
            Give a binary score 'yes' or 'no'. Yes, means that the answer resolves the question otherwise return 'no'
            """)
        Score invoke(String userMessage);
    }

    @Inject
    Service service;

    public Score gradeAnswer(Arguments args) {
        Prompt prompt = StructuredPromptProcessor.toPrompt(args);

        return service.invoke(prompt.text());
    }

}
