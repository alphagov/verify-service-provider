package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslateSamlResponseBody {
    private final String samlResponse;
    private final String requestId;

    @JsonCreator
    public TranslateSamlResponseBody(@JsonProperty("samlResponse") String samlResponse,
                                     @JsonProperty("requestId") String requestId) {
        this.samlResponse = samlResponse;
        this.requestId = requestId;
    }

    public String getSamlResponse() {
        return samlResponse;
    }

    public String getRequestId() {
        return requestId;
    }
}
