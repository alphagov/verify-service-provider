package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.core.domain.NonMatchingAttributes;

public class TestTranslatedNonMatchingResponseBody extends TranslatedNonMatchingResponseBody {

    @JsonCreator
    public TestTranslatedNonMatchingResponseBody(
            @JsonProperty("scenario") NonMatchingScenario scenario,
            @JsonProperty("pid") String pid,
            @JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance,
            @JsonProperty("attributes") NonMatchingAttributes attributes
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
