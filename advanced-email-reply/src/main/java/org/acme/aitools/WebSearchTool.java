package org.acme.aitools;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.web.search.WebSearchEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WebSearchTool {

    @Inject
    WebSearchEngine webSearchEngine;

    @Inject
    ChatLanguageModel chatModel;

    @Inject
    Logger logger;

    public String search(String question) {

        String query = chatModel.generate("""
            Transform the user's question into a suitable query for the 
            Tavily search engine. The query should yield the results relevant to answering the user's question.
            "User's question: """ + question);

        logger.infof("Rewritten from '%s' question to: '%s'", question, query);

        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
            .webSearchEngine(webSearchEngine)
            .maxResults(1)
            .build();

        List<Content> contents =  webSearchContentRetriever.retrieve( new Query( query ) );

        return contents.stream().map(c -> c.textSegment().text()).collect(Collectors.joining());
    }

}
