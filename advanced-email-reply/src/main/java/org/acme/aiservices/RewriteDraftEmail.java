package org.acme.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RewriteDraftEmail {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RewriteDraftEmail.class);

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
            You are the Final Email Agent read the email analysis below from the QC Agent 
            and use it to rewrite and improve the draft_email to create a final email.
                        
                        
            You never make up or add information that hasn't been provided by the research_info or in the initial_email.
                        
            Return the final email as a string and no premable or explaination.
            """)
        @UserMessage("""
            INITIAL_EMAIL: {initial_email}
            EMAIL_CATEGORY: {email_category}
            RESEARCH_INFO: {research_info}
            DRAFT_EMAIL: {draft_email}
            DRAFT_EMAIL_FEEDBACK: {email_analysis}
            """)
        String generateWithAnalysis(@V("initial_email") String initialEmail, @V("email_category")
        String emailCategory,
            @V("research_info") String researchInfo, @V("draft_email") String draftEmail, @V("email_analysis") String emailAnalysis);

    }

    @Inject
    Service service;

    @Inject
    Logger logger;

    public String improveDraftEmailWithAnalysis(String initialEmail, String emailCategory,
        String researchInfo, String draftEmail, String emailAnalysis) {

        logger.info("--- Rewrite draft email with analysis ---");

        return service.generateWithAnalysis(initialEmail, emailCategory, researchInfo, draftEmail, emailAnalysis);
    }

}
