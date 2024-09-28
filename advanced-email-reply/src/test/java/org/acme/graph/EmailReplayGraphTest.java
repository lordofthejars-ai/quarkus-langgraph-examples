package org.acme.graph;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.bsc.langgraph4j.CompiledGraph;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class EmailReplayGraphTest {

    @Inject
    EmailReplayGraph graph;

    static String feedbackEmail = """
        HI there,
        I am emailing to say that I had a wonderful stay at your resort last week.

        I really appreciate what your staff did

        Thanks,
        Paul
        """;

    @Test
    public void shouldResponseFromAFeedbackEmail() throws Exception {
        String email = graph.generateAnswer(feedbackEmail);
        assertThat(email).isNotBlank().containsIgnoringCase("Paul");
    }
}
