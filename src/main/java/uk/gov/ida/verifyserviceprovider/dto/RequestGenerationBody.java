package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestGenerationBody {

    private final LevelOfAssurance levelOfAssurance;
    private final String entityId;

    public RequestGenerationBody(String entityId) {
        this.levelOfAssurance = null;
        this.entityId = entityId;
    }

    @JsonCreator
    public RequestGenerationBody(
        @JsonProperty("levelOfAssurance") LevelOfAssurance levelOfAssurance,
        @JsonProperty("entityId") String entityId) {
        this.levelOfAssurance = levelOfAssurance;
        this.entityId = entityId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getEntityId() {
        return entityId;
    }
}
