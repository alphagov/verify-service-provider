package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;
import org.joda.time.Duration;
import uk.gov.ida.verifyserviceprovider.dto.ServiceDetails;

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
    @Valid
    private List<ServiceDetails> services;

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

    @JsonProperty
    @NotNull
    @Valid
    private Duration clockSkew;

    public List<ServiceDetails> getServices() {
        return services;
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

    public Duration getClockSkew() {
        return clockSkew;
    }
}
