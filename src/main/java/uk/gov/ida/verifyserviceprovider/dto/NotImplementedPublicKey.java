package uk.gov.ida.verifyserviceprovider.dto;

import java.security.PrivateKey;
import java.security.PublicKey;

public class NotImplementedPublicKey implements PublicKey {
    private final String algorithm;
    private final String format;

    public NotImplementedPublicKey(PrivateKey privateKey) {
        this.algorithm = privateKey.getAlgorithm();
        this.format = privateKey.getFormat();
    }

    @Override
    public String getAlgorithm() {
        return this.algorithm;
    }

    @Override
    public String getFormat() {
        return this.format;
    }

    @Override
    public byte[] getEncoded() {
        throw new RuntimeException("Not implemented");
    }
}
