package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

import static java.util.Optional.ofNullable;

public class VerifyHubConfiguration {

    private URI hubSsoLocation;
    private HubMetadataConfiguration hubMetadataConfiguration;

    @JsonCreator
    public VerifyHubConfiguration(
        @JsonProperty("environment") HubEnvironment hubEnvironment,
        @JsonProperty("ssoLocation") URI hubSsoLocation,
        @JsonProperty("metadata") HubMetadataConfiguration hubMetadataConfiguration
    ) {
        this.hubSsoLocation = ofNullable(hubSsoLocation).orElse(hubEnvironment.getSsoLocation());
        this.hubMetadataConfiguration = ofNullable(hubMetadataConfiguration).orElse(createHubMetadataConfigurationWithDefaults());
        this.hubMetadataConfiguration.setEnvironment(hubEnvironment);
    }

    private HubMetadataConfiguration createHubMetadataConfigurationWithDefaults() {
        return new HubMetadataConfiguration(null, null, null, null, null, null, null, null, null, null);
    }

    public URI getHubSsoLocation() {
        return hubSsoLocation;
    }

    public HubMetadataConfiguration getHubMetadataConfiguration() {
        return hubMetadataConfiguration;
    }
}
