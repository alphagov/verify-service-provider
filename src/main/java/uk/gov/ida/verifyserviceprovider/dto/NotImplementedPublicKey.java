package uk.gov.ida.verifyserviceprovider.dto;

import java.security.PublicKey;

public class NotImplementedPublicKey implements PublicKey {
    @Override
    public String getAlgorithm() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getFormat() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public byte[] getEncoded() {
        throw new RuntimeException("Not implemented");
    }
}
