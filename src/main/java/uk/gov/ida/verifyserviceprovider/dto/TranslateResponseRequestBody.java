package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslateResponseRequestBody {
    public final String authnResponse;
    public final String secureToken;

    @JsonCreator
    public TranslateResponseRequestBody(@JsonProperty("authnResponse") String authnResponse,
                                        @JsonProperty("secureToken") String secureToken) {
        this.authnResponse = authnResponse;
        this.secureToken = secureToken;
    }
}
