package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Optional;

public class NonMatchingVerifiableAttribute<T> {

    private final T value;
    private final boolean verified;
    private final Optional<LocalDateTime> from;
    private final Optional<LocalDateTime> to;

    @JsonCreator
    public NonMatchingVerifiableAttribute(@JsonProperty("value") T value,
                               @JsonProperty("verified") boolean verified,
                               @JsonProperty("from") Optional<LocalDateTime> from,
                               @JsonProperty("to") Optional<LocalDateTime> to) {
        this.value = value;
        this.verified = verified;
        this.from = from;
        this.to = to;
    }

    public T getValue() {
        return value;
    }

    public boolean isVerified() {
        return verified;
    }

    public Optional<LocalDateTime> getFrom() { return from; }

    public Optional<LocalDateTime> getTo() { return to; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NonMatchingVerifiableAttribute<?> that = (NonMatchingVerifiableAttribute<?>) o;

        return isVerified() == that.isVerified() &&
                getValue().equals(that.getValue()) &&
                getFrom().equals(that.getFrom()) &&
                getTo().equals(that.getTo());
    }

    @Override
    public int hashCode() {
        int result = getValue().hashCode();
        result = 31 * result + (isVerified() ? 1 : 0);
        result = 31 * result + getFrom().hashCode();
        result = 31 * result + getTo().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("NonMatchingVerifiableAttribute{ value=%s, verified=%s, from=%s, to=%s }", value, verified, from, to);
    }

}
