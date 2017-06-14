package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestGenerationBody {

    public final LevelOfAssurance levelOfAssurance;

    @JsonCreator
    public RequestGenerationBody(@JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance) {
        this.levelOfAssurance = levelOfAssurance;
    }

}
