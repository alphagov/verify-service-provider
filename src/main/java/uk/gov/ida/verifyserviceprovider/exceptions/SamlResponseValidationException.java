package uk.gov.ida.verifyserviceprovider.exceptions;

public class SamlResponseValidationException extends RuntimeException {

    public SamlResponseValidationException(String message) {
        super(message);
    }
}
