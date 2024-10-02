package org.acme.aitools;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.web.search.WebSearchEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.jboss.logging.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class WebSearchTool {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WebSearchTool.class);
    @Inject
    WebSearchEngine webSearchEngine;

    @Inject
    ChatLanguageModel chatModel;

    @Inject
    Logger logger;

    public List<Content> search(String question) {

        String query = chatModel.generate("""
            Transform the user's question into a suitable query for the 
            Tavily search engine. The query should yield the results relevant to answering the user's question.
            "User's question: """ + question);

        logger.infof("Rewritten from '%s' question to: '%s'", question, query);

        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
            .webSearchEngine(webSearchEngine)
            .maxResults(3)
            .build();

        return webSearchContentRetriever.retrieve( new Query( query ) );

    }

}
