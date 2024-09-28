package org.acme.aiservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;

@QuarkusTest
public class ExtractQuestionsTest {

    @Inject
    ExtractQuestionsEmail extractQuestionsEmail;

    static String question = """
        HI there, 
        I am emailing to say that the resort weather was way to cloudy and overcast. 
        I wanted to write a song called 'Here comes the sun but it never came'
                
        What should be the weather in Arizona in April?
                
        I really hope you fix this next time.
                
        Thanks,
        George
        """;

    @Inject
    ObjectMapper mapper;

    @Test
    public void shouldExtractQuestionsFromEmail() throws JsonProcessingException {

        JsonNode questions = extractQuestionsEmail.findQuestions(question, "customer_feedback");
        DocumentContext ctx = JsonPath.parse(mapper.writeValueAsString(questions));

        assertThat(ctx).jsonPathAsString("$.questions[0]")
            .isEqualTo("What should be the weather in Arizona in April?");

    }

}
