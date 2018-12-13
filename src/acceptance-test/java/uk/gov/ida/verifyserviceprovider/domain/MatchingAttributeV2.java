package uk.gov.ida.verifyserviceprovider.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class MatchingAttributeV2 {
    @JsonProperty
    private String value;
    @JsonProperty @JsonInclude(Include.NON_NULL)
    private LocalDateTime from;
    @JsonProperty @JsonInclude(Include.NON_NULL)
    private LocalDateTime to;
    @JsonProperty
    private boolean verified;

    public MatchingAttributeV2(
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