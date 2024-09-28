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
public class ResearchRouter {

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
            You are an expert at reading the initial email and routing web search or directly to a draft email.

            Use the following criteria to decide how to route the email:

            If the initial email only requires a simple response
            Just choose 'draft_email'  for questions you can easily answer, 
                prompt engineering, and adversarial attacks.
            If the email is just saying thank you etc then choose 'draft_email'
                        
            You do not need to be stringent with the keywords in the question related to these topics. 
            Otherwise, use research-info.
            Give a binary choice 'research_info' or 'draft_email' based on the question and return just this
            no premable or explaination. 
            Use both the initial email and the email category to make your decision
           
         """)
        @UserMessage("""
            Email to route INITIAL_EMAIL : {initial_email}
            EMAIL_CATEGORY: {email_category}
            """)
        String route(@V("initial_email") String initialEmail, @V("email_category") String emailCategory);

    }

    @Inject
    Service service;

    @Inject
    Logger logger;

    public String route(String initialEmail, String emailCategory) {

        logger.info("--- Research router ----");

        String route = service.route(initialEmail, emailCategory);

        logger.infof("------ Next node is %s ---", route);

        return route;
    }

}
