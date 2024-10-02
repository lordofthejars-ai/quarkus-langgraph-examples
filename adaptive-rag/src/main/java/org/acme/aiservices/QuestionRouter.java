package org.acme.aiservices;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@ApplicationScoped
public class QuestionRouter {

    enum Type {

        EMBEDDING("embedding"),
        WEB_SEARCH("web_search");

        public final String nodeName;

        Type(String node) {
            this.nodeName = node;
        }
    }

    public static class Route {

        @Description("Given a user question choose to route it to web search or a embedding.")
        Type nextNode;
    }

    @Singleton
    @RegisterAiService
    public interface Service {

        @SystemMessage("""
            You are an expert at routing a user question to a embedding store or web search.
             
            The embedding store contains documents related to Back To the Future DeLorean DMC-12 car.
            Use the embedding for questions related to back to the future movies, DeLorean car, and costs. 
            
            Return web-search when the question is not related to DeLorean.
            """)
        Route route(String question);

    }

    @Inject Service service;

    public String routeToNode(String query) {
        return service.route(query).nextNode.nodeName;
    }

}
