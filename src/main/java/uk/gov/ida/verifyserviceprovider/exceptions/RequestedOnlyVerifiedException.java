package uk.gov.ida.verifyserviceprovider.exceptions;

public class RequestedOnlyVerifiedException extends SamlResponseValidationException {
    public RequestedOnlyVerifiedException() {
        super("Invalid attributes request: Cannot request verification status without requesting attribute value");
    }
}
