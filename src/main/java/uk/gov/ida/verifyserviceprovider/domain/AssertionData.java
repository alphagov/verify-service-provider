package uk.gov.ida.verifyserviceprovider.domain;

import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.MatchingDataset;

public class AssertionData {

    private AuthnContext levelOfAssurance;
    private MatchingDataset matchingDataset;

    public AssertionData(AuthnContext levelOfAssurance, MatchingDataset matchingDataset) {
        this.levelOfAssurance = levelOfAssurance;
        this.matchingDataset = matchingDataset;
    }

    public MatchingDataset getMatchingDataset() {
        return matchingDataset;
    }

    public AuthnContext getLevelOfAssurance() {
        return levelOfAssurance;
    }

}