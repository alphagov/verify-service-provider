package uk.gov.ida.verifyserviceprovider.compliance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.ida.saml.core.domain.AuthnContext;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class MatchingDataset {
    @NotNull
    @Valid
    @JsonProperty
    private MatchingAttribute firstName;

    @NotNull
    @Valid
    @JsonProperty
    private MatchingAttribute middleNames;

    @NotNull
    @Valid
    @JsonProperty
    @NotEmpty
    private List<MatchingAttribute> surnames;


    @JsonInclude(Include.NON_NULL)
    @Valid
    @JsonProperty
    private MatchingAttribute gender;

    @NotNull
    @Valid
    @JsonProperty
    private MatchingAttribute dateOfBirth;

    @Valid
    @JsonProperty
    @JsonInclude(Include.NON_NULL)
    private List<MatchingAddress> addresses;

    @NotNull
    @Valid
    @JsonProperty
    private AuthnContext levelOfAssurance;

    @NotNull
    @Valid
    @JsonProperty
    private String persistentId;

    public MatchingDataset() {}

    public MatchingDataset(MatchingAttribute firstName,
                           MatchingAttribute middleNames,
                           List<MatchingAttribute> surnames,
                           MatchingAttribute gender,
                           MatchingAttribute dateOfBirth,
                           List<MatchingAddress> addresses,
                           AuthnContext levelOfAssurance,
                           String persistentId) {
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.addresses = addresses;
        this.levelOfAssurance = levelOfAssurance;
        this.persistentId = persistentId;
    }

    public MatchingAttribute getFirstName() {
        return firstName;
    }

    public MatchingAttribute getMiddleNames() {
        return middleNames;
    }

    public List<MatchingAttribute> getSurnames() {
        return surnames;
    }

    public MatchingAttribute getGender() {
        return gender;
    }

    public MatchingAttribute getDateOfBirth() {
        return dateOfBirth;
    }

    public List<MatchingAddress> getAddresses() {
        return addresses;
    }

    public AuthnContext getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getPersistentId() {
        return persistentId;
    }

}
