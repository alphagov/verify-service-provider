package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class TranslateSamlResponseBody {
    private final String samlResponse;
    private final String requestId;
    private final LevelOfAssurance levelOfAssurance;
    private final String entityId;

    @JsonCreator
    public TranslateSamlResponseBody(
        @JsonProperty(value = "samlResponse") String samlResponse,
        @JsonProperty(value = "requestId") String requestId,
        @JsonProperty(value = "levelOfAssurance") LevelOfAssurance levelOfAssurance,
        @JsonProperty(value = "entityId") String entityId
    ) {
        this.samlResponse = samlResponse;
        this.requestId = requestId;
        this.levelOfAssurance = levelOfAssurance;
        this.entityId = entityId;
    }

    @NotNull
    public String getSamlResponse() {
        return samlResponse;
    }

    @NotNull
    public String getRequestId() {
        return requestId;
    }

    @NotNull
    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getEntityId() { return entityId; }
}
