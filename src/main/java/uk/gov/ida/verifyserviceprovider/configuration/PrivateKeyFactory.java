package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.security.PrivateKey;

/* There is a bug being tracked here - https://github.com/FasterXML/jackson-databind/issues/1358
preventing us from deserializing directly to one of the JsonSubTypes when there is a defaultImpl
defined. This can be avoided by only ever to deserialize to the super type (PrivateKeyFactory) */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    property = "type",
    defaultImpl = InlineEncodedKeyFactory.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrivateKeyFileConfiguration.class, name = "file"),
    @JsonSubTypes.Type(value = InlineEncodedKeyFactory.class, name = "inline"),
})
public abstract class PrivateKeyFactory {
    public abstract PrivateKey getPrivateKey();

    protected PrivateKey getPrivateKeyFromBytes(byte[] privateKey) {
        uk.gov.ida.common.shared.security.PrivateKeyFactory privateKeyFactory = new uk.gov.ida.common.shared.security.PrivateKeyFactory();
        return privateKeyFactory.createPrivateKey(privateKey);
    }
}
