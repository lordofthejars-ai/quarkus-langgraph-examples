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
public class CategorizeEmail {


    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
            You are a Email Categorizer Agent You are a master at understanding 
            what a customer wants when they write an email and are able to categorize 
            it in a useful way.
            """)
        @UserMessage(""" 
            Conduct a comprehensive analysis of the email provided and categorize into one of the following categories:
              price_equiry - used when someone is asking for information about pricing 
              customer_complaint - used when someone is complaining about something 
              product_enquiry - used when someone is asking for information about a product feature, benefit or service but not about pricing 
              customer_feedback - used when someone is giving feedback about a product 
              off_topic when it doesn't relate to any other category 
                                
                                
           Output a single category only from the types ('price_equiry', 'customer_complaint', 'product_enquiry', 'customer_feedback', 'off_topic') eg: 'price_enquiry'
                                
           EMAIL CONTENT: {initial_email}
                                
            """)
        String generate(@V("initial_email") String initialEmail);
    }

    @Inject
    Service service;

    @Inject
    Logger logger;

    public String categorize(String initialEmail) {
        logger.info("--- Categorizing Initial Email ---");

        String category = service.generate(initialEmail);

        logger.infof("---  Category detected %s ---", category);

        return category;
    }

}
