package org.acme.aiservices;

import dev.langchain4j.model.output.structured.Description;

public class Score {

    @Description("Answer addresses the question, 'true' or 'false'")
    public boolean binaryScore;
}
