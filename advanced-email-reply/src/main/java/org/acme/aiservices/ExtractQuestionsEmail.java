package org.acme.aiservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ExtractQuestionsEmail {

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
            You are a master at working out to get questions from given text.

            given the INITIAL_EMAIL and EMAIL_CATEGORY. Extract all the questions written in the email.

            Return a JSON with a single key 'questions' with no more than 3 questions and no premable or explaination.
                        
            """)
        @UserMessage("""
            INITIAL_EMAIL: {initial_email}
            EMAIL_CATEGORY: {email_category}
            """)
        String findQuestions(@V("initial_email") String initialEmail, @V("email_category") String emailCategory);

    }

    @Inject
    Service service;

    @Inject
    Logger logger;

    @Inject
    ObjectMapper mapper;

    public JsonNode findQuestions(String initialEmail, String emailCategory) {

        logger.info("--- Find questions in Email ----");

        String json =  service.findQuestions(initialEmail, emailCategory);
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
