package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslatedMatchingResponseBody extends TranslatedResponseBody {
    @JsonCreator
    public TranslatedMatchingResponseBody(
            @JsonProperty("scenario")
                    MatchingScenario scenario,
            @JsonProperty("pid")
                    String pid,
            @JsonProperty("levelOfAssurance")
                    LevelOfAssurance levelOfAssurance,
            @JsonProperty("attributes")
                    UserAccountCreationAttributes attributes
    ) {
        super(scenario, pid, levelOfAssurance, attributes);
    }
}
