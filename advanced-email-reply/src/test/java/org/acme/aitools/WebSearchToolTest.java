package org.acme.aitools;

import dev.langchain4j.rag.content.Content;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class WebSearchToolTest {

    @Inject
    WebSearchTool webSearchTool;

    @Test
    public void shouldFindInformationAboutWeather() {

        final String contents = webSearchTool.search("What should be the weather in Arizona, USA in April?");

        assertThat(contents)
            .containsIgnoringCase("Weather in Arizona, USA");
    }

}
