package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.verifyserviceprovider.exceptions.FailedToRequestVerifiedException;
import uk.gov.ida.verifyserviceprovider.exceptions.RequestedOnlyVerifiedException;

import java.util.Optional;

public class VerifiableAttributeV1<T> {

    private final T value;
    private final boolean verified;

    @JsonCreator
    public VerifiableAttributeV1(@JsonProperty("value") T value,
                                 @JsonProperty("verified") boolean verified) {
        this.value = value;
        this.verified = verified;
    }

    public static <Y> VerifiableAttributeV1<Y> fromOptionals(Optional<Y> value, Optional<Boolean> verified) {
        if (value.isPresent() && verified.isPresent()) {
            return new VerifiableAttributeV1<>(value.get(), verified.get());
        }

        if (!value.isPresent() && !verified.isPresent()) {
            return null;
        }

        if (value.isPresent() && !verified.isPresent()) {
            throw new FailedToRequestVerifiedException();
        }

        if (!value.isPresent() && verified.isPresent()) {
            throw new RequestedOnlyVerifiedException();
        }

        return null;
    }

    public T getValue() {
        return value;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerifiableAttributeV1<?> that = (VerifiableAttributeV1<?>) o;

        return isVerified() == that.isVerified() && getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        int result = getValue().hashCode();
        result = 31 * result + (isVerified() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("VerifiableAttributeV1{ value=%s, verified=%s }", value, verified);
    }
}
