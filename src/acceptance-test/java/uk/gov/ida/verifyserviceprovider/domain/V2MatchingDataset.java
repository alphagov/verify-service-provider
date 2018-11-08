package uk.gov.ida.verifyserviceprovider.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class V2MatchingDataset {
    @JsonProperty
    private MatchingAttribute firstName;
    @JsonProperty
    private MatchingAttribute middleNames;
    @JsonProperty
    private List<MatchingAttribute> surnames;
    @JsonProperty
    private MatchingAttribute gender;
    @JsonProperty
    private MatchingAttribute dateOfBirth;
    @JsonProperty
    private List<MatchingAttribute> addresses;
    @JsonProperty
    private String persistentId;

    public V2MatchingDataset(MatchingAttribute firstName,
                             MatchingAttribute middleNames,
                             List<MatchingAttribute> surnames,
                             MatchingAttribute gender,
                             MatchingAttribute dateOfBirth,
                             List<MatchingAttribute> addresses,
                             String persistentId) {
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.surnames = surnames;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.addresses = addresses;
        this.persistentId = persistentId;
    }
}
