package uk.gov.ida.verifyserviceprovider.dto;

public enum NonMatchingScenario implements Scenario {
    IDENTITY_VERIFIED,
    CANCELLATION,
    AUTHENTICATION_FAILED,
    REQUEST_ERROR
}
