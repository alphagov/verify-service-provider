package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class HistoricalVerifiableAttribute<T> {

    @JsonProperty("value")
    private final T value;
    @JsonProperty("verified")
    private final boolean verified;
    @JsonProperty("from") @JsonInclude(JsonInclude.Include.NON_NULL)
    private final LocalDate from;
    @JsonProperty("to") @JsonInclude(JsonInclude.Include.NON_NULL)
    private final LocalDate to;

    public HistoricalVerifiableAttribute(
           T value,
           boolean verified,
           LocalDate from,
           LocalDate to) {
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

    public LocalDate getFrom() { return from; }

    public LocalDate getTo() { return to; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoricalVerifiableAttribute<?> that = (HistoricalVerifiableAttribute<?>) o;

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
        return String.format("HistoricalVerifiableAttribute{ value=%s, verified=%s, from=%s, to=%s }", value, verified, from, to);
    }

}
