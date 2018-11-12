package uk.gov.ida.verifyserviceprovider.exceptions;

public class AuthnContextMissingException extends RuntimeException {
    public AuthnContextMissingException(String message) {
        super(message);
    }
}
