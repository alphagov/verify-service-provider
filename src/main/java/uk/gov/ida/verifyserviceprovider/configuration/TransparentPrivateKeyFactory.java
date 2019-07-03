package uk.gov.ida.verifyserviceprovider.configuration;

import java.security.PrivateKey;

public class TransparentPrivateKeyFactory extends PrivateKeyFactory {

    private PrivateKey aNull;

    public TransparentPrivateKeyFactory(PrivateKey key) {
        this.aNull = key;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return aNull;
    }
}
