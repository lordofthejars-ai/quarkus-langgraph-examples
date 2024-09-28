package org.acme.aiservices;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class RewriteRouterTest {

    @Inject
    RewriteRouter rewriteRouter;

    @Test
    public void shouldRewriteAnEmail() {

        String decision = rewriteRouter.route(EmailMotherObject.EMAIL, "customer_feedback", "Yo we can't help you, best regards Sarah");
        assertThat(decision).isEqualTo("rewrite");
    }

}
