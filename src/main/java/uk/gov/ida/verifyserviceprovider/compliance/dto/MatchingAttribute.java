package uk.gov.ida.verifyserviceprovider.compliance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class MatchingAttribute {
    @NotNull
    @JsonProperty
    private String value;
    @JsonProperty @JsonInclude(Include.NON_NULL)
    private LocalDateTime from;
    @JsonProperty @JsonInclude(Include.NON_NULL)
    private LocalDateTime to;
    @NotNull
    @JsonProperty
    private boolean verified;

    public MatchingAttribute() {};

    public String getValue() {
        return value;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public boolean isVerified() {
        return verified;
    }

    public MatchingAttribute(String value) {
        this.value = value;
        this.verified = true;
    }

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