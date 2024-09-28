package org.acme.aiservices;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class WriteDraftEmailTest {

    @Inject
    WriteDraftEmail writeDraftEmail;

    @Test
    public void shouldDraftAnEmail() {

        String draft = writeDraftEmail.writeDraftEmail(EmailMotherObject.EMAIL, "customer_feedback", "");

        assertThat(draft).isNotBlank().containsIgnoringCase("Sarah");

    }


}
