package org.acme.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RewriteRouter {

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
            You are an expert at evaluating the emails that are draft emails for the customer and deciding if they
                need to be rewritten to be better.

                Use the following criteria to decide if the DRAFT_EMAIL needs to be rewritten: 
                        
                If the INITIAL_EMAIL only requires a simple response which the DRAFT_EMAIL contains then it doesn't need to be rewritten.
                If the DRAFT_EMAIL addresses all the concerns of the INITIAL_EMAIL then it doesn't need to be rewritten.
                If the DRAFT_EMAIL is missing information that the INITIAL_EMAIL requires then it needs to be rewritten.
                        
                Give a binary choice 'rewrite' (for needs to be rewritten) or 'no_rewrite' (for doesn't need to be rewritten) based on the DRAFT_EMAIL and the criteria.
                Return the string with no premable or explaination.
            """)
        @UserMessage("""
            INITIAL_EMAIL: {initial_email}
                EMAIL_CATEGORY: {email_category}
                DRAFT_EMAIL: {draft_email}
            """)
        String route(@V("initial_email") String initialEmail,
            @V("email_category") String emailCategory,
            @V("draft_email") String draftEmail);

    }

    @Inject
    Service service;

    @Inject
    Logger logger;

    public String route(String initialEmail, String emailCategory, String draftEmail) {

        logger.info("--- Rewrite Route ----");
        String route = service.route(initialEmail, emailCategory, draftEmail);
        logger.infof("------ Next node is %s ---", route);

        return route;
    }

}
