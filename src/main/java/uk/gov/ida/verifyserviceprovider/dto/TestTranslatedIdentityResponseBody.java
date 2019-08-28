package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestTranslatedIdentityResponseBody extends TranslatedIdentityResponseBody {

    @JsonCreator
    public TestTranslatedIdentityResponseBody(
            @JsonProperty("scenario") IdentityScenario scenario,
            @JsonProperty("pid") String pid,
            @JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance,
            @JsonProperty("attributes") IdentityAttributes attributes
    ) {
        super(scenario, pid, levelOfAssurance, attributes);
    }

    public String getPid() {
        return pid;
    }

    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }
}
