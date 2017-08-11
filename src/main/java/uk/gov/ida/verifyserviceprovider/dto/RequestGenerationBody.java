package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class RequestGenerationBody {

    private final LevelOfAssurance levelOfAssurance;

    @JsonCreator
    public RequestGenerationBody(@JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance) {
        this.levelOfAssurance = levelOfAssurance;
    }

    @NotNull
    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }
}
