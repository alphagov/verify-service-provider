package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;
import org.joda.time.Duration;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.security.PrivateKey;
import java.util.List;

public class VerifyServiceProviderConfiguration extends Configuration {

    public static final String NOT_EMPTY_MESSAGE = "may not be empty";

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private List<String> serviceEntityIds;

    @JsonProperty
    @NotNull
    @Valid
    private VerifyHubConfiguration verifyHubConfiguration;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlSigningKey;

    @JsonProperty
    @NotNull
    @Valid
    private DeserializablePublicKeyConfiguration samlPrimarySigningCert;

    @JsonProperty
    @Valid
    private DeserializablePublicKeyConfiguration samlSecondarySigningCert = null;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlPrimaryEncryptionKey;

    @JsonProperty
    @NotNull
    @Valid
    private DeserializablePublicKeyConfiguration samlPrimaryEncryptionCert;

    @JsonProperty
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlSecondaryEncryptionKey;

    @JsonProperty
    @NotNull
    @Valid
    private MsaMetadataConfiguration msaMetadata;

    @JsonProperty
    @NotNull
    @Valid
    private Duration clockSkew;

    public List<String> getServiceEntityIds() {
        return serviceEntityIds;
    }

    public URI getHubSsoLocation() {
        return verifyHubConfiguration.getHubSsoLocation();
    }

    public PrivateKey getSamlSigningKey() {
        return samlSigningKey;
    }

    public PrivateKey getSamlPrimaryEncryptionKey() {
        return samlPrimaryEncryptionKey;
    }

    public PrivateKey getSamlSecondaryEncryptionKey() {
        return samlSecondaryEncryptionKey;
    }

    public MsaMetadataConfiguration getMsaMetadata() {
        return msaMetadata;
    }

    public HubMetadataConfiguration getVerifyHubMetadata() {
        return verifyHubConfiguration.getHubMetadataConfiguration();
    }

    public Duration getClockSkew() {
        return clockSkew;
    }

    public DeserializablePublicKeyConfiguration getSamlPrimarySigningCert() {
        return samlPrimarySigningCert;
    }

    public DeserializablePublicKeyConfiguration getSamlPrimaryEncryptionCert() {
        return samlPrimaryEncryptionCert;
    }

    public DeserializablePublicKeyConfiguration getSamlSecondarySigningCert() {
        return samlSecondarySigningCert;
    }
}
