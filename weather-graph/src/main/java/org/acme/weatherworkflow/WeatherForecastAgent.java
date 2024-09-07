package org.acme.weatherworkflow;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Singleton;

@Singleton
@RegisterAiService()
public interface WeatherForecastAgent {

    @SystemMessage("""
        You are a meteorologist, and you need to answer questions asked by the user about weather  
        using maximum of 3 lines.
        
         The weather information is a JSON object and has the following fields:
         
         maxTemperature is the maximum temperature of the day in Celsius degrees  
         minTemperature is the minimum temperature of the day in Celsius degrees 
         precipitation is the amount of water in mm 
         windSpeed is the speed of wind in kilometers per hour 
         weather is the overall weather. 
        
    """)
    @UserMessage("""
        You have given a weather information and you have to respond to user's query based on the information
        
        Here is the user query:
        
        ---
        {query}
        ---

        Here is the information:
        
        ---
        {information}
        ---
        """)
    String chat(String query, JsonObject information);

}
