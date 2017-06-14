package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class RequestResponseBody {

    public final String samlRequest;
    public final String secureToken;
    public final URI location;

    @JsonCreator
    public RequestResponseBody(@JsonProperty("samlRequest") String samlRequest,
                               @JsonProperty("secureToken") String secureToken,
                               @JsonProperty("location") URI location) {
        this.samlRequest = samlRequest;
        this.secureToken = secureToken;
        this.location = location;
    }
}
