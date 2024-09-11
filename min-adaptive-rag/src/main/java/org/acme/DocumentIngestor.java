package org.acme;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;

import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkiverse.langchain4j.chroma.ChromaEmbeddingStore;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;

@ApplicationScoped
public class DocumentIngestor {

    @Inject
    ChromaEmbeddingStore store;

    @Inject
    AllMiniLmL6V2EmbeddingModel embeddingModel;

    @Inject
    Logger logger;

    @Startup
    public void ingestDocuments() {
        ingest(Arrays.asList("delorean-spec.txt", "delorean-costs.txt", "delorean-facts.txt", "delorean-maintenance.txt"));
    }

    void ingest(List<String> classpaths) {

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .embeddingStore(store)
            .embeddingModel(embeddingModel)
            .documentSplitter(recursive(500, 0))
            .build();

        final List<Document> documents = classpaths.stream()
            .map(this::createDocument)
            .toList();
        ingestor.ingest(documents);
    }

    Document createDocument(String classpathLocation) {
        URL document = this.getClass().getClassLoader().getResource(classpathLocation);
        logger.infof("Document %s to process.", classpathLocation);
        if (document != null) {
            logger.infof("Document %s processed.", classpathLocation);
            return UrlDocumentLoader.load(document, new TextDocumentParser());
        }

        return new Document("");
    }

}
