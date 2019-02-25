package uk.gov.ida.verifyserviceprovider.dto;

import java.time.LocalDate;

public class NonMatchingVerifiableAttributeBuilder {
    private String value = "VALUE";
    private boolean verified = true;
    private LocalDate from = LocalDate.now();
    private LocalDate to = null;

    public NonMatchingVerifiableAttributeBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public NonMatchingVerifiableAttributeBuilder withFrom(LocalDate from) {
        this.from = from;
        return this;
    }

    public NonMatchingVerifiableAttributeBuilder withTo(LocalDate to) {
        this.to = to;
        return this;
    }

    public NonMatchingVerifiableAttribute<String> build() {
        return new NonMatchingVerifiableAttribute<>(value, verified, from, to);
    }
}