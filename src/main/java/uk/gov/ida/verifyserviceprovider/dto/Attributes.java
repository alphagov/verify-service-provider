package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Attributes {

    private final VerifiableAttribute<String> firstName;
    private final VerifiableAttribute<String>  middleName;
    private final VerifiableAttribute<String>  surname;
    private final VerifiableAttribute<LocalDate> dateOfBirth;
    private final VerifiableAttribute<Address> address;
    private final List<VerifiableAttribute<Address>> addressHistory;
    private final String cycle3;

    @JsonCreator
    public Attributes(
        @JsonProperty("firstName") VerifiableAttribute<String> firstName,
        @JsonProperty("middleName") VerifiableAttribute<String> middleName,
        @JsonProperty("surname") VerifiableAttribute<String> surname,
        @JsonProperty("dateOfBirth") VerifiableAttribute<LocalDate> dateOfBirth,
        @JsonProperty("address") VerifiableAttribute<Address> address,
        @JsonProperty("addressHistory") List<VerifiableAttribute<Address>> addressHistory,
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

    public VerifiableAttribute<String> getFirstName() {
        return firstName;
    }

    public VerifiableAttribute<String> getMiddleName() {
        return middleName;
    }

    public VerifiableAttribute<String> getSurname() {
        return surname;
    }

    public VerifiableAttribute<LocalDate> getDateOfBirth() {
        return dateOfBirth;
    }

    public VerifiableAttribute<Address> getAddress() {
        return address;
    }

    public List<VerifiableAttribute<Address>> getAddressHistory() {
        return addressHistory;
    }

    public String getCycle3() {
        return cycle3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attributes that = (Attributes) o;
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
