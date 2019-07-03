package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.validation.ValidationMethod;
import jersey.repackaged.com.google.common.cache.CacheLoader;

import java.security.PrivateKey;
import java.util.Base64;

@JsonTypeName("inline")
public class InlineEncodedKeyFactory extends PrivateKeyFactory {
    private final String value;

    private CacheLoader<String, PrivateKey> loader = CacheLoader.from(key -> getPrivateKeyFromValue());;

    public InlineEncodedKeyFactory(String value) {
        this.value = value;
//        this.privateKey = getPrivateKeyFromValue(value);
    }

    @Override
    public PrivateKey getPrivateKey() {
        return loadPrivateKey();
    }

    @ValidationMethod(message =
        "A private key is not loadable. Keys must be provided as base64 encoded PKCS8 RSA private keys")
    @JsonIgnore @SuppressWarnings(value = "unused")
    public boolean isKeyLoadable() {
       return loadPrivateKey() != null;
    }

    private PrivateKey loadPrivateKey() {
        try {
            return loader.load("key");
        } catch (Exception e) {
            return null;
        }
    }

    private PrivateKey getPrivateKeyFromValue() {
        return getPrivateKeyFromBytes(Base64.getDecoder().decode(value));
    }
}
