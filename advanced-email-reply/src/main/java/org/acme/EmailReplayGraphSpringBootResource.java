package org.acme;

import org.acme.graph.EmailReplayGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello")
public class EmailReplayGraphSpringBootResource {

    private final EmailReplayGraph graph;

    @Autowired
    public EmailReplayGraphSpringBootResource(EmailReplayGraph graph) {
        this.graph = graph;
    }

    private static final String feedbackEmail = """
        HI there,
        I am emailing to say that I had a wonderful stay at your resort last week.

        I really appreciate what your staff did

        Thanks,
        Paul
        """;

    @GetMapping(value = "/feedback", produces = "text/plain")
    public String generate() {
        return graph.generateAnswer(feedbackEmail);
    }

    private static final String questionEmail = """
        Hi there, 
        I am emailing to say that the resort weather was way too cloudy and overcast. 
        I wanted to write a song called 'Here comes the sun but it never came'
                
        What should be the weather in Arizona in April?
                
        I really hope you fix this next time.
                
        Thanks,
        George
        """;

    @GetMapping(value = "/question", produces = "text/plain")
    public String answer() {
        return graph.generateAnswer(questionEmail);
    }
}
