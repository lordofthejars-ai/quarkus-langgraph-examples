package org.acme.aiservices;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;

@QuarkusTest
public class DraftEmailAnalysisTest {

    @Inject
    DraftEmailAnalysis draftEmailAnalysis;

    @Test
    public void shouldAnalyzeAnEmail() {
        String emailAnalysis = draftEmailAnalysis.analyze(EmailMotherObject.EMAIL,
            "customer_feedback",
            "", "Yo we can't help you, best regards Sarah");

        DocumentContext ctx = JsonPath.parse(emailAnalysis);

        assertThat(ctx).jsonPathAsBoolean("$.draft_analysis.issues_addressed")
            .isFalse();
        assertThat(ctx).jsonPathAsString("$.draft_analysis.feedback")
            .containsIgnoringCase("does not address the customer's feedback");


    }

}
