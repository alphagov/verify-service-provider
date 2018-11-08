package uk.gov.ida.verifyserviceprovider.exceptions;

public class NoHashingEntityIdIsProvidedError extends RuntimeException {
    public NoHashingEntityIdIsProvidedError(String message) {
        super(message);
    }
}
