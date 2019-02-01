package uk.gov.ida.verifyserviceprovider.compliance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MatchingAddress {

    @JsonProperty
    private boolean verified;
    @JsonProperty
    private LocalDateTime from;
    @JsonProperty
    private LocalDateTime to;
    @JsonProperty
    private Optional<String> postCode;
    @JsonProperty
    private List<String> lines;
    @JsonProperty
    private Optional<String> internationalPostCode;
    @JsonProperty
    private Optional<String> uprn;

    public MatchingAddress() {}

    public MatchingAddress(
        final boolean verified,
        final LocalDateTime from,
        final LocalDateTime to,
        String postCode,
        List<String> lines,
        String internationalPostCode,
        String uprn
    ) {
        this.verified = verified;
        this.from = from;
        this.to = to;
        this.postCode = Optional.ofNullable(postCode);
        this.lines = lines;
        this.internationalPostCode = Optional.ofNullable(internationalPostCode);
        this.uprn = Optional.ofNullable(uprn);
    }

    public boolean isVerified() {
        return verified;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public Optional<String> getPostCode() {
        return postCode;
    }

    public List<String> getLines() {
        return lines;
    }

    public Optional<String> getInternationalPostCode() {
        return internationalPostCode;
    }

    public Optional<String> getUprn() {
        return uprn;
    }
}
