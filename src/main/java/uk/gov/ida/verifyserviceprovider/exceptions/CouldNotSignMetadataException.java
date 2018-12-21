package uk.gov.ida.verifyserviceprovider.exceptions;

public class CouldNotSignMetadataException extends RuntimeException {
    public CouldNotSignMetadataException(Exception e) {
        super(e);
    }
}
