package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.security.PrivateKey;

public class VerifyServiceProviderConfiguration extends Configuration {

    public static final String NOT_EMPTY_MESSAGE = "may not be empty";

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String serviceEntityId;

    @JsonProperty
    @NotNull
    @Valid
    private URI hubSsoLocation;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlSigningKey;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlPrimaryEncryptionKey;

    @JsonProperty
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlSecondaryEncryptionKey;

    @JsonProperty
    @NotNull
    @Valid
    private MetadataConfigurationWithHubDefaults verifyHubMetadata;

    @JsonProperty
    @NotNull
    @Valid
    private MetadataConfigurationWithMsaDefaults msaMetadata;

    public String getServiceEntityId() {
        return serviceEntityId;
    }

    public URI getHubSsoLocation() {
        return hubSsoLocation;
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

    public MetadataConfigurationWithMsaDefaults getMsaMetadata() {
        return msaMetadata;
    }

    public MetadataConfigurationWithHubDefaults getVerifyHubMetadata() {
        return verifyHubMetadata;
    }
}
