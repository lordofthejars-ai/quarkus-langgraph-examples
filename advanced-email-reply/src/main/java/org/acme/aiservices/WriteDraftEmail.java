package org.acme.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WriteDraftEmail {

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
             You are the Email Writer Agent take the INITIAL_EMAIL below  from a human that has emailed our company email address, 
             the email_category 
             that the categorizer agent gave it and the research from the research agent and 
             write a helpful email in a thoughtful and friendly way.
                                     
             If the customer email is 'off_topic' then ask them questions to get more information.
             If the customer email is 'customer_complaint' then try to assure we value them and that we are addressing their issues.
             If the customer email is 'customer_feedback' then try to assure we value them and that we are addressing their issues.
             If the customer email is 'product_enquiry' then try to give them the info the researcher provided in a succinct and friendly way.
             If the customer email is 'price_equiry' then try to give the pricing info they requested.
                        
             You never make up information that hasn't been provided by the research_info or in the initial_email.
             Always sign off the emails in appropriate manner and from Sarah the Resident Manager.
                        
             Return the email as text with no premable or explaination.
            """)
        @UserMessage("""
            INITIAL_EMAIL: {initial_email}
            EMAIL_CATEGORY: {email_category}
            RESEARCH_INFO: {research_info} 
            """)
        String draftMessage(@V("initial_email") String initialEmail,
            @V("email_category") String emailCategory,
            @V("research_info") String researchInfo);
    }

    @Inject
    Service service;

    @Inject
    Logger logger;

    public String writeDraftEmail(String initialEmail, String emailCategory, List<String> researchInfos) {
        return this.writeDraftEmail(initialEmail, emailCategory, String.join(" ", researchInfos));
    }

    public String writeDraftEmail(String initialEmail, String emailCategory, String researchInfo) {

        logger.info("--- Write draft email ---");

        return service.draftMessage(initialEmail, emailCategory, researchInfo);
    }

}
