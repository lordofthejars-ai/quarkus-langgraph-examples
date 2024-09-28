package org.acme.aiservices;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class RewriteDraftEmailTest {

    @Inject
    RewriteDraftEmail rewriteDraftEmail;

    static String analysis = """
        {'draft_analysis': {'issues_addressed': False,
                            'feedback': ['The draft email does not '
                                                        "acknowledge the customer's "
                                                        'positive feedback, instead, '
                                                        'it seems to be dismissive and '
                                                        'unhelpful. A more appropriate '
                                                        'response would be to thank '
                                                        'the customer for their '
                                                        'feedback and express '
                                                        'appreciation for their stay. '
                                                        'The tone should be friendly '
                                                        'and welcoming. The response '
                                                        'should also include a '
                                                        'personalized touch, '
                                                        'addressing the customer by '
                                                        'their name.']}}
        """;

    @Test
    public void shouldRewriteTheEmailWithTheAnalysis() {
        String email = rewriteDraftEmail.improveDraftEmailWithAnalysis(
            EmailMotherObject.EMAIL, "customer_feedback", "", "Yo we can't help you, best regards Sarah",
        analysis);

        Assertions.assertThat(email).containsIgnoringCase("Thank you");
    }

}
