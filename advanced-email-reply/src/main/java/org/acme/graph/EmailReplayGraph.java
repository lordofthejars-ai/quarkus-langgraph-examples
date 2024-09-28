package org.acme.graph;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;
import org.bsc.langgraph4j.CompiledGraph;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailReplayGraph {

    @Inject
    CompiledGraph<EmailReplayGraphProducer.State> graph;

    @Inject
    Logger logger;

    public String generateAnswer(String email) {

        try {
            final Optional<EmailReplayGraphProducer.State> optionalState = graph.invoke(Map.of("initial_email", email));

            final EmailReplayGraphProducer.State state =
                optionalState.orElseThrow(() -> new IllegalArgumentException("Couldn't process the email automatically"));

            logger.infof("-- Final state: %s ---", state.finalEmail());

            return state.finalEmail();

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
