package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslateSamlResponseBody {
    public final String samlResponse;
    public final String secureToken;

    @JsonCreator
    public TranslateSamlResponseBody(@JsonProperty("samlResponse") String samlResponse,
                                     @JsonProperty("secureToken") String secureToken) {
        this.samlResponse = samlResponse;
        this.secureToken = secureToken;
    }
}
