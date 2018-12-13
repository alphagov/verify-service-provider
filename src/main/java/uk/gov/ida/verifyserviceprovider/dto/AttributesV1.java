package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AttributesV1 {

    private final VerifiableAttributeV1<String> firstName;
    private final VerifiableAttributeV1<String>  middleName;
    private final VerifiableAttributeV1<String>  surname;
    private final VerifiableAttributeV1<LocalDate> dateOfBirth;
    private final VerifiableAttributeV1<AddressV1> address;
    private final List<VerifiableAttributeV1<AddressV1>> addressHistory;
    private final String cycle3;

    @JsonCreator
    public AttributesV1(
        @JsonProperty("firstName") VerifiableAttributeV1<String> firstName,
        @JsonProperty("middleName") VerifiableAttributeV1<String> middleName,
        @JsonProperty("surname") VerifiableAttributeV1<String> surname,
        @JsonProperty("dateOfBirth") VerifiableAttributeV1<LocalDate> dateOfBirth,
        @JsonProperty("address") VerifiableAttributeV1<AddressV1> address,
        @JsonProperty("addressHistory") List<VerifiableAttributeV1<AddressV1>> addressHistory,
        @JsonProperty("cycle3") String cycle3
    ) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.addressHistory = addressHistory;
        this.cycle3 = cycle3;
    }

    public VerifiableAttributeV1<String> getFirstName() {
        return firstName;
    }

    public VerifiableAttributeV1<String> getMiddleName() {
        return middleName;
    }

    public VerifiableAttributeV1<String> getSurname() {
        return surname;
    }

    public VerifiableAttributeV1<LocalDate> getDateOfBirth() {
        return dateOfBirth;
    }

    public VerifiableAttributeV1<AddressV1> getAddress() {
        return address;
    }

    public List<VerifiableAttributeV1<AddressV1>> getAddressHistory() {
        return addressHistory;
    }

    public String getCycle3() {
        return cycle3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttributesV1 that = (AttributesV1) o;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        if (middleName != null ? !middleName.equals(that.middleName) : that.middleName != null) return false;
        if (surname != null ? !surname.equals(that.surname) : that.surname != null) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(that.dateOfBirth) : that.dateOfBirth != null) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (addressHistory != null ? !(that.addressHistory != null && CollectionUtils.isEqualCollection(addressHistory, that.addressHistory)) : that.addressHistory != null) return false;
        return cycle3 != null ? cycle3.equals(that.cycle3) : that.cycle3 == null;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (addressHistory != null ? addressHistory.hashCode() : 0);
        result = 31 * result + (cycle3 != null ? cycle3.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
            "Attributes{ firstName=%s, middleName=%s, surname=%s, dateOfBirth=%s, address=%s, addressHistory=%s, cycle3=%s}",
            firstName, middleName, surname, dateOfBirth, address, addressHistory, cycle3);
    }

}
