package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AddressV2 {

    private final List<String> lines;
    private final String postCode;
    private final String internationalPostCode;

    @JsonCreator
    public AddressV2(
        @JsonProperty("lines") List<String> lines,
        @JsonProperty("postCode") String postCode,
        @JsonProperty("internationalPostCode") String internationalPostCode
    ) {
        this.lines = lines;
        this.postCode = postCode;
        this.internationalPostCode = internationalPostCode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddressV2 other = (AddressV2) o;

        if (lines != null ? !lines.equals(other.lines) : other.lines != null) return false;
        if (postCode != null ? !postCode.equals(other.postCode) : other.postCode != null) return false;
        return (internationalPostCode != null ? !internationalPostCode.equals(other.internationalPostCode) : other.internationalPostCode != null);
    }

    @Override
    public int hashCode() {
        int result = lines != null ? lines.hashCode() : 0;
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        result = 31 * result + (internationalPostCode != null ? internationalPostCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Address{" +
            ", lines=" + lines +
            ", postCode='" + postCode + '\'' +
            ", internationalPostCode='" + internationalPostCode + '\'' +
            '}';
    }

}
