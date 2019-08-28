package uk.gov.ida.verifyserviceprovider.dto;

public enum IdentityScenario implements Scenario {
    IDENTITY_VERIFIED,
    NO_AUTHENTICATION,
    AUTHENTICATION_FAILED,
    REQUEST_ERROR
}
