package org.acme.weatherworkflow;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.inject.Singleton;

@Singleton
@RegisterAiService
public interface CityExtractorAgent {

    String NO_CITY = "no_response";

    @UserMessage("""
        You are given one question and you have to extract city name from it
        Only reply the city name if it exists or reply 'no_response' if there is no city name in question

        Here is the question: {msg}
        """)
    String extractCity(String msg);

}
