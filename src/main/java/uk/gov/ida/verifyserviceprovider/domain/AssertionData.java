package uk.gov.ida.verifyserviceprovider.domain;

import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.MatchingDataset;

public class AssertionData {

    private String matchingDatasetIssuer;
    private AuthnContext levelOfAssurance;
    private MatchingDataset matchingDataset;

    public AssertionData(String matchingDatasetIssuer, AuthnContext levelOfAssurance, MatchingDataset matchingDataset) {
        this.matchingDatasetIssuer = matchingDatasetIssuer;
        this.levelOfAssurance = levelOfAssurance;
        this.matchingDataset = matchingDataset;
    }

    public MatchingDataset getMatchingDataset() {
        return matchingDataset;
    }

    public AuthnContext getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getMatchingDatasetIssuer() {
        return matchingDatasetIssuer;
    }

}