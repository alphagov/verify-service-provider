package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslateResponseRequestBody {
    public final String response;
    public final String secureToken;

    @JsonCreator
    public TranslateResponseRequestBody(@JsonProperty("response") String response,
                                        @JsonProperty("secureToken") String secureToken) {
        this.response = response;
        this.secureToken = secureToken;
    }
}
