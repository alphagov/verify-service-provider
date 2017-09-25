package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class RequestGenerationBody {

    private final LevelOfAssurance levelOfAssurance;
    private final String entityId;

    @JsonCreator
    public RequestGenerationBody(
        @JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance,
        @JsonProperty("entityId") String entityId) {
        this.levelOfAssurance = levelOfAssurance;
        this.entityId = entityId;
    }

    @NotNull
    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getEntityId() {
        return entityId;
    }
}
