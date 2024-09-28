package org.acme.aiservices;


import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class ResearchRouterTest {


    @Inject
    ResearchRouter researchRouter;

    @Test
    public void shouldRouteToDraftEmailWhenCustomerFeedback() {
        String route = researchRouter.route(EmailMotherObject.EMAIL, "customer_feedback");
        assertThat(route).isEqualTo("draft_email");
    }


}
