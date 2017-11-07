package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;
import org.joda.time.Duration;

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
    private PrivateKey signingKey;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey primaryEncryptionKey;

    @JsonProperty
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey secondaryEncryptionKey;

    @JsonProperty
    @NotNull
    @Valid
    private MatchingServiceAdapterConfiguration matchingServiceAdapter;

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

    public PrivateKey getSigningKey() {
        return signingKey;
    }

    public PrivateKey getPrimaryEncryptionKey() {
        return primaryEncryptionKey;
    }

    public PrivateKey getSecondaryEncryptionKey() {
        return secondaryEncryptionKey;
    }

    public MatchingServiceAdapterConfiguration getMatchingServiceAdapter() {
        return matchingServiceAdapter;
    }

    public HubMetadataConfiguration getVerifyHubMetadata() {
        return verifyHubConfiguration.getHubMetadataConfiguration();
    }

    public Duration getClockSkew() {
        return clockSkew;
    }
}
