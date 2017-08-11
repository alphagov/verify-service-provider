package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class Address {

    private final List<String> lines;
    private final String postCode;
    private final String internationalPostCode;
    private final String uprn;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    @JsonCreator
    public Address(
        @JsonProperty("lines") List<String> lines,
        @JsonProperty("postCode") String postCode,
        @JsonProperty("internationalPostCode") String internationalPostCode,
        @JsonProperty("uprn") String uprn,
        @JsonProperty("fromDate") LocalDate fromDate,
        @JsonProperty("toDate") LocalDate toDate
    ) {
        this.lines = lines;
        this.postCode = postCode;
        this.internationalPostCode = internationalPostCode;
        this.uprn = uprn;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public List<String> getLines() {
        return lines;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getInternationalPostCode() {
        return internationalPostCode;
    }

    public String getUprn() {
        return uprn;
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

        Address address = (Address) o;

        if (lines != null ? !lines.equals(address.lines) : address.lines != null) return false;
        if (postCode != null ? !postCode.equals(address.postCode) : address.postCode != null) return false;
        if (internationalPostCode != null ? !internationalPostCode.equals(address.internationalPostCode) : address.internationalPostCode != null)
            return false;
        if (uprn != null ? !uprn.equals(address.uprn) : address.uprn != null) return false;
        if (fromDate != null ? !fromDate.equals(address.fromDate) : address.fromDate != null) return false;
        return toDate != null ? toDate.equals(address.toDate) : address.toDate == null;
    }

    @Override
    public int hashCode() {
        int result = lines != null ? lines.hashCode() : 0;
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        result = 31 * result + (internationalPostCode != null ? internationalPostCode.hashCode() : 0);
        result = 31 * result + (uprn != null ? uprn.hashCode() : 0);
        result = 31 * result + (fromDate != null ? fromDate.hashCode() : 0);
        result = 31 * result + (toDate != null ? toDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Address{" +
            ", lines=" + lines +
            ", postCode='" + postCode + '\'' +
            ", internationalPostCode='" + internationalPostCode + '\'' +
            ", uprn='" + uprn + '\'' +
            ", fromDate=" + fromDate +
            ", toDate=" + toDate +
            '}';
    }

}
