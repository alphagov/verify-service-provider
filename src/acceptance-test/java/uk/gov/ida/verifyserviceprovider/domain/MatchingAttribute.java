package uk.gov.ida.verifyserviceprovider.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class MatchingAttribute {

    @JsonProperty
    private String value;
    @JsonProperty
    private LocalDateTime from;
    @JsonProperty
    private LocalDateTime to;
    @JsonProperty
    private boolean verified;

    public MatchingAttribute(
            final String value,
            final boolean verified,
            final LocalDateTime from,
            final LocalDateTime to) {

        this.value = value;
        this.verified = verified;
        this.from = from;
        this.to = to;
    }
}
