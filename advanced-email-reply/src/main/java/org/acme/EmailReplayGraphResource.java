package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.graph.EmailReplayGraph;

@Path("/hello")
public class EmailReplayGraphResource {

    @Inject
    EmailReplayGraph graph;

    static String feedbackEmail = """
        HI there,
        I am emailing to say that I had a wonderful stay at your resort last week.

        I really appreciate what your staff did

        Thanks,
        Paul
        """;

    @GET
    @Path("/feedback")
    @Produces(MediaType.TEXT_PLAIN)
    public String generate() {
        return graph.generateAnswer(feedbackEmail);
    }

    static String questionEmail = """
        Hi there, 
        I am emailing to say that the resort weather was way to cloudy and overcast. 
        I wanted to write a song called 'Here comes the sun but it never came'
                
        What should be the weather in Arizona in April?
                
        I really hope you fix this next time.
                
        Thanks,
        George
        """;

    @GET
    @Path("/question")
    @Produces(MediaType.TEXT_PLAIN)
    public String answer() {
        return graph.generateAnswer(questionEmail);
    }

}
