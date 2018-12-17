package uk.gov.ida.verifyserviceprovider.exceptions;

import static java.text.MessageFormat.format;

public class TrustStoreLoadingException extends RuntimeException {
    public TrustStoreLoadingException(String trustStorePath) {
        super(format("Could not load truststore from path {0}", trustStorePath));
    }
}
