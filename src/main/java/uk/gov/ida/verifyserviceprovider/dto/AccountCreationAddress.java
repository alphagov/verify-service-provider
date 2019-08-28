package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class AccountCreationAddress extends Address {

    private final LocalDate fromDate;
    private final LocalDate toDate;

    @JsonCreator
    public AccountCreationAddress(
        @JsonProperty("lines") List<String> lines,
        @JsonProperty("postCode") String postCode,
        @JsonProperty("internationalPostCode") String internationalPostCode,
        @JsonProperty("uprn") String uprn,
        @JsonProperty("fromDate") LocalDate fromDate,
        @JsonProperty("toDate") LocalDate toDate
    ) {
        super(lines, postCode, internationalPostCode, uprn);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccountCreationAddress that = (AccountCreationAddress) o;
        return Objects.equals(fromDate, that.fromDate) &&
            Objects.equals(toDate, that.toDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fromDate, toDate);
    }
}
