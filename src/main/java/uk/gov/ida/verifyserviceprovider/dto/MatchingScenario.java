package uk.gov.ida.verifyserviceprovider.dto;

public enum MatchingScenario implements Scenario {
    SUCCESS_MATCH,
    ACCOUNT_CREATION,
    NO_MATCH,
    CANCELLATION,
    AUTHENTICATION_FAILED,
    REQUEST_ERROR
}
