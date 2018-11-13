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
        this.hubSsoLocation = ofNullable(hubSsoLocation).orElseGet(() -> hubEnvironment.getSsoLocation());
        this.hubMetadataConfiguration = ofNullable(hubMetadataConfiguration).orElseGet(() -> hubEnvironment.getMetadataConfiguration());
    }

    public URI getHubSsoLocation() {
        return hubSsoLocation;
    }

    public HubMetadataConfiguration getHubMetadataConfiguration() {
        return hubMetadataConfiguration;
    }
}
