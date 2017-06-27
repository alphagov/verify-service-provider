package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class Attributes {

    private final String firstName;
    private final boolean firstNameVerified;
    private final String middleName;
    private final boolean middleNameVerified;
    private final String surname;
    private final boolean surnameVerified;
    private final LocalDate dateOfBirth;
    private final boolean dateOfBirthVerified;
    private final Address address;
    private final String cycle3;

    @JsonCreator
    public Attributes(
        @JsonProperty("firstName") String firstName,
        @JsonProperty("firstNameVerified") boolean firstNameVerified,
        @JsonProperty("middleName") String middleName,
        @JsonProperty("middleNameVerified") boolean middleNameVerified,
        @JsonProperty("surname") String surname,
        @JsonProperty("surnameVerified") boolean surnameVerified,
        @JsonProperty("dateOfBirth") LocalDate dateOfBirth,
        @JsonProperty("dateOfBirthVerified") boolean dateOfBirthVerified,
        @JsonProperty("address") Address address,
        @JsonProperty("cycle3") String cycle3
    ) {
        this.firstName = firstName;
        this.firstNameVerified = firstNameVerified;
        this.middleName = middleName;
        this.middleNameVerified = middleNameVerified;
        this.surname = surname;
        this.surnameVerified = surnameVerified;
        this.dateOfBirth = dateOfBirth;
        this.dateOfBirthVerified = dateOfBirthVerified;
        this.address = address;
        this.cycle3 = cycle3;
    }

    public String getFirstName() {
        return firstName;
    }

    public boolean isFirstNameVerified() {
        return firstNameVerified;
    }

    public String getMiddleName() {
        return middleName;
    }

    public boolean isMiddleNameVerified() {
        return middleNameVerified;
    }

    public String getSurname() {
        return surname;
    }

    public boolean isSurnameVerified() {
        return surnameVerified;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public boolean isDateOfBirthVerified() {
        return dateOfBirthVerified;
    }

    public Address getAddress() {
        return address;
    }

    public String getCycle3() {
        return cycle3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attributes that = (Attributes) o;

        if (firstNameVerified != that.firstNameVerified) return false;
        if (middleNameVerified != that.middleNameVerified) return false;
        if (surnameVerified != that.surnameVerified) return false;
        if (dateOfBirthVerified != that.dateOfBirthVerified) return false;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        if (middleName != null ? !middleName.equals(that.middleName) : that.middleName != null) return false;
        if (surname != null ? !surname.equals(that.surname) : that.surname != null) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(that.dateOfBirth) : that.dateOfBirth != null) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        return cycle3 != null ? cycle3.equals(that.cycle3) : that.cycle3 == null;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (firstNameVerified ? 1 : 0);
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (middleNameVerified ? 1 : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (surnameVerified ? 1 : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (dateOfBirthVerified ? 1 : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (cycle3 != null ? cycle3.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Attributes{" +
            "firstName='" + firstName + '\'' +
            ", firstNameVerified=" + firstNameVerified +
            ", middleName='" + middleName + '\'' +
            ", middleNameVerified=" + middleNameVerified +
            ", surname='" + surname + '\'' +
            ", surnameVerified=" + surnameVerified +
            ", dateOfBirth=" + dateOfBirth +
            ", dateOfBirthVerified=" + dateOfBirthVerified +
            ", address=" + address +
            ", cycle3='" + cycle3 + '\'' +
            '}';
    }

}
