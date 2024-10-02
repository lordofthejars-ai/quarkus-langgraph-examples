package org.acme.aitools;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import io.quarkiverse.langchain4j.chroma.ChromaEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmbeddingStoreRetrieval {

    @Inject
    ChromaEmbeddingStore store;

    @Inject
    AllMiniLmL6V2EmbeddingModel embeddingModel;

    public EmbeddingSearchResult<TextSegment> search(String query) {

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding( queryEmbedding )
            .maxResults( 1 )
            .minScore( 0.0 )
            .build();
        return store.search( searchRequest );

    }

}
