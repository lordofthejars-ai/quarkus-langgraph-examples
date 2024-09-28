package org.acme.aiservices;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class CategorizeEmailTest {

    @Inject
    CategorizeEmail categorizeEmail;

    @Test
    public void shouldCategorizeEmailAsCustomerFeedback() {

        final String emailCategory = categorizeEmail.categorize(EmailMotherObject.EMAIL);

        assertThat(emailCategory).isEqualTo("customer_feedback");

    }

}
