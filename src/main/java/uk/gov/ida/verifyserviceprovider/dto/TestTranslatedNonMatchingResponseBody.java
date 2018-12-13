package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class TestTranslatedNonMatchingResponseBody extends TranslatedNonMatchingResponseBody {

    @JsonCreator
    public TestTranslatedNonMatchingResponseBody(
            @JsonProperty("scenario") NonMatchingScenario scenario,
            @JsonProperty("pid") String pid,
            @JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance,
            @JsonProperty("attributes") AttributesV2 attributes
    ) {
        super(scenario, pid, levelOfAssurance, attributes);
    }

    public NonMatchingScenario getScenario() {
        return super.getScenario();
    }

    public String getPid() {
        return pid;
    }

    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public Optional<AttributesV2> getAttributes() {
        return Optional.ofNullable(attributes);
    }

}
