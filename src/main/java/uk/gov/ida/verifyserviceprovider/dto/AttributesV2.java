package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.ida.saml.core.domain.Gender;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributesV2 {

    @JsonProperty("firstName")
    private final VerifiableAttributeV2<String> firstName;
    @JsonProperty("middleNames")
    private final List<VerifiableAttributeV2<String>> middleNames;
    @JsonProperty("surnames")
    private final List<VerifiableAttributeV2<String>> surnames;
    @JsonProperty("dateOfBirth")
    private final VerifiableAttributeV2<LocalDate> dateOfBirth;
    @JsonProperty("gender")
    private final VerifiableAttributeV2<Gender> gender;
    @JsonProperty("addresses")
    private final List<VerifiableAttributeV2<AddressV2>> addresses;


    public AttributesV2(
            VerifiableAttributeV2<String> firstName,
            List<VerifiableAttributeV2<String>> middleNames,
            List<VerifiableAttributeV2<String>> surnames,
            VerifiableAttributeV2<LocalDate> dateOfBirth,
            VerifiableAttributeV2<Gender> gender,
            List<VerifiableAttributeV2<AddressV2>> addresses
    ) {
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.addresses = addresses;
    }

    public VerifiableAttributeV2<String> getFirstName() {
        return firstName;
    }

    public List<VerifiableAttributeV2<String>> getMiddleNames() {
        return middleNames;
    }

    public List<VerifiableAttributeV2<String>> getSurnames() {
        return surnames;
    }

    public VerifiableAttributeV2<LocalDate> getDateOfBirth() {
        return dateOfBirth;
    }

    public VerifiableAttributeV2<Gender> getGender() {
        return gender;
    }

    public List<VerifiableAttributeV2<AddressV2>> getAddresses() {
        return addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttributesV2 that = (AttributesV2) o;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        if (middleNames != null ? !(that.middleNames != null && CollectionUtils.isEqualCollection(middleNames, that.middleNames)) : that.middleNames != null) return false;
        if (surnames != null ? !(that.surnames != null && CollectionUtils.isEqualCollection(surnames, that.surnames)) : that.surnames != null) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(that.dateOfBirth) : that.dateOfBirth != null) return false;
        if (gender != null ? !gender.equals(that.gender) : that.gender != null) return false;
        return (addresses != null ? !(that.addresses != null && CollectionUtils.isEqualCollection(addresses, that.addresses)) : that.addresses != null);
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (middleNames != null ? middleNames.hashCode() : 0);
        result = 31 * result + (surnames != null ? surnames.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (addresses != null ? addresses.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "Attributes{ firstName=%s, middleNames=%s, surnames=%s, dateOfBirth=%s, gender=%s, addresses=%s}",
                firstName, middleNames, surnames, dateOfBirth, gender, addresses);
    }

}
