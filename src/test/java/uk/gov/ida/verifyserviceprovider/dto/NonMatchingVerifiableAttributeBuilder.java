package uk.gov.ida.verifyserviceprovider.dto;

import java.time.LocalDateTime;

public class NonMatchingVerifiableAttributeBuilder {
    private String value = "VALUE";
    private boolean verified = true;
    private LocalDateTime from = LocalDateTime.now();
    private LocalDateTime to = null;

    public NonMatchingVerifiableAttributeBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public NonMatchingVerifiableAttributeBuilder withFrom(LocalDateTime from) {
        this.from = from;
        return this;
    }

    public NonMatchingVerifiableAttributeBuilder withTo(LocalDateTime to) {
        this.to = to;
        return this;
    }

    public NonMatchingVerifiableAttribute<String> build() {
        return new NonMatchingVerifiableAttribute<>(value, verified, from, to);
    }
}