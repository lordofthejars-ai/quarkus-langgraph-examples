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
public class DraftEmailAnalysis {

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
            You are the Quality Control Agent read the INITIAL_EMAIL below  from a human that has emailed
                our company email address, the email_category that the categorizer agent gave it and the 
                research from the research agent and write an analysis of how the email.
                        
                Check if the DRAFT_EMAIL addresses the customer's issued based on the email category and the 
                content of the initial email.
                        
                Give feedback of how the email can be improved and what specific things can be added or change
                to make the email more effective at addressing the customer's issues.
                        
                You never make up or add information that hasn't been provided by the research_info or in the initial_email.
                        
                Return the analysis a JSON with a root element 'draft_analysis' , having 'issues_addressed' key as boolean setting true if it has been addressed or false otherwise
                and key 'feedback' with the feedback and no premable or explanation.
                T
            """)
        @UserMessage("""
            INITIAL_EMAIL: {initial_email}
            EMAIL_CATEGORY: {email_category}
            RESEARCH_INFO: {research_info}
            DRAFT_EMAIL: {draft_email}
            """)
        String analyze(@V("initial_email") String initialEmail, @V("email_category") String emailCategory,
            @V("research_info") String researchInfo,@V("draft_email") String draftEmail);
    }

    @Inject
    Service service;

    @Inject
    Logger logger;

    public String analyze(String initialEmail, String emailCategory, String researchInfo, String draftEmail) {

        logger.info("--- Analyzing Draft Email ---");

        return service.analyze(initialEmail, emailCategory, researchInfo, draftEmail);
    }

}
