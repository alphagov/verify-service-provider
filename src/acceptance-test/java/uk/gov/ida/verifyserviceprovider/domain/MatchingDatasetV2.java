package uk.gov.ida.verifyserviceprovider.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MatchingDatasetV2 {
    @JsonProperty
    private MatchingAttributeV2 firstName;
    @JsonProperty
    private MatchingAttributeV2 middleNames;
    @JsonProperty
    private List <MatchingAttributeV2> surnames;
    @JsonProperty
    private MatchingAttributeV2 gender;
    @JsonProperty
    private MatchingAttributeV2 dateOfBirth;
    @JsonProperty
    private List<MatchingAddressV2> addresses;
    @JsonProperty
    private String persistentId;

    public MatchingDatasetV2(MatchingAttributeV2 firstName,
                             MatchingAttributeV2 middleNames,
                             List <MatchingAttributeV2> surnames,
                             MatchingAttributeV2 gender,
                             MatchingAttributeV2 dateOfBirth,
                             List<MatchingAddressV2> addresses,
                             String persistentId) {
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.addresses = addresses;
        this.persistentId = persistentId;
    }

    public void setPersisentId(String persisentId) {
        this.persistentId = persisentId;
    }
}
