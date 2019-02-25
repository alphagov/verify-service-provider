package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.ida.saml.core.domain.Gender;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NonMatchingAttributes {

    @JsonProperty("firstNames")
    private final List<NonMatchingTransliterableAttribute> firstNames;
    @JsonProperty("middleNames")
    private final List<NonMatchingVerifiableAttribute<String>> middleNames;
    @JsonProperty("surnames")
    private final List<NonMatchingTransliterableAttribute> surnames;
    @JsonProperty("datesOfBirth")
    private final List<NonMatchingVerifiableAttribute<LocalDate>> datesOfBirth;
    @JsonProperty("gender")
    private final NonMatchingVerifiableAttribute<Gender> gender;
    @JsonProperty("addresses")
    private final List<NonMatchingVerifiableAttribute<NonMatchingAddress>> addresses;


    @JsonCreator
    public NonMatchingAttributes(
            @JsonProperty("firstNames")
            List<NonMatchingTransliterableAttribute> firstNames,
            @JsonProperty("middleNames")
            List<NonMatchingVerifiableAttribute<String>> middleNames,
            @JsonProperty("surnames")
            List<NonMatchingTransliterableAttribute> surnames,
            @JsonProperty("datesOfBirth")
            List<NonMatchingVerifiableAttribute<LocalDate>> datesOfBirth,
            @JsonProperty("gender")
            NonMatchingVerifiableAttribute<Gender> gender,
            @JsonProperty("addresses")
            List<NonMatchingVerifiableAttribute<NonMatchingAddress>> addresses
    ) {
        this.firstNames = firstNames;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.datesOfBirth = datesOfBirth;
        this.gender = gender;
        this.addresses = addresses;
    }

    public List<NonMatchingTransliterableAttribute> getFirstNames() {
        return firstNames;
    }

    public List<NonMatchingVerifiableAttribute<String>> getMiddleNames() {
        return middleNames;
    }

    public List<NonMatchingTransliterableAttribute> getSurnames() {
        return surnames;
    }

    public List<NonMatchingVerifiableAttribute<LocalDate>> getDatesOfBirth() {
        return datesOfBirth;
    }

    public NonMatchingVerifiableAttribute<Gender> getGender() {
        return gender;
    }

    public List<NonMatchingVerifiableAttribute<NonMatchingAddress>> getAddresses() {
        return addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NonMatchingAttributes that = (NonMatchingAttributes) o;
        if (firstNames != null ? !firstNames.equals(that.firstNames) : that.firstNames != null) return false;
        if (middleNames != null ? !(that.middleNames != null && CollectionUtils.isEqualCollection(middleNames, that.middleNames)) : that.middleNames != null) return false;
        if (surnames != null ? !(that.surnames != null && CollectionUtils.isEqualCollection(surnames, that.surnames)) : that.surnames != null) return false;
        if (datesOfBirth != null ? !datesOfBirth.equals(that.datesOfBirth) : that.datesOfBirth != null) return false;
        if (gender != null ? !gender.equals(that.gender) : that.gender != null) return false;
        return (addresses != null ? !(that.addresses != null && CollectionUtils.isEqualCollection(addresses, that.addresses)) : that.addresses != null);
    }

    @Override
    public int hashCode() {
        int result = firstNames != null ? firstNames.hashCode() : 0;
        result = 31 * result + (middleNames != null ? middleNames.hashCode() : 0);
        result = 31 * result + (surnames != null ? surnames.hashCode() : 0);
        result = 31 * result + (datesOfBirth != null ? datesOfBirth.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (addresses != null ? addresses.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "Attributes{ firstNames=%s, middleNames=%s, surnames=%s, datesOfBirth=%s, gender=%s, addresses=%s}",
                firstNames, middleNames, surnames, datesOfBirth, gender, addresses);
    }

}
