package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslateSamlResponseBody {
    public final String response;
    public final String secureToken;

    @JsonCreator
    public TranslateSamlResponseBody(@JsonProperty("response") String response,
                                     @JsonProperty("secureToken") String secureToken) {
        this.response = response;
        this.secureToken = secureToken;
    }
}
