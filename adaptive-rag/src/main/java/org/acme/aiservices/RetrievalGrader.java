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
public class RetrievalGrader {

    @StructuredPrompt("""
        Retrieved document: {{document}} User question: {{question}}
    """)
    public static class Arguments {
        String question;
        String document;

        public Arguments(String question, String document) {
            this.question = question;
            this.document = document;
        }
    }

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
            You are a grader assessing relevance of a retrieved document to a user question.
            If the document contains keyword(s) or semantic meaning related to the user question, grade it as relevant.
            It does not need to be a stringent test. The goal is to filter out erroneous retrievals. 
            Give a binary score 'yes' or 'no' score to indicate whether the document is relevant to the question.
            """)
        Score invoke(String question);

    }

    @Inject
    Service service;

    public Score gradeDocument(Arguments args) {
        Prompt prompt = StructuredPromptProcessor.toPrompt(args);
        return service.invoke(prompt.text());
    }

}
