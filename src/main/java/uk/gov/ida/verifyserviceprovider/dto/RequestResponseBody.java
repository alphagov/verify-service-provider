package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class RequestResponseBody {
    private final String samlRequest;
    private final String requestId;
    private final URI ssoLocation;

    @JsonCreator
    public RequestResponseBody(@JsonProperty("samlRequest") String samlRequest,
                               @JsonProperty("requestId") String requestId,
                               @JsonProperty("ssoLocation") URI ssoLocation) {
        this.samlRequest = samlRequest;
        this.requestId = requestId;
        this.ssoLocation = ssoLocation;
    }

    public String getSamlRequest() {
        return samlRequest;
    }

    public String getRequestId() {
        return requestId;
    }

    public URI getSsoLocation() {
        return ssoLocation;
    }
}
