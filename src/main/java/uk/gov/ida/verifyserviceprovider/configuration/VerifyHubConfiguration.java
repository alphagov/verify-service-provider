package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

import static java.util.Optional.ofNullable;

public class VerifyHubConfiguration {

    private HubEnvironment hubEnvironment;
    private URI hubSsoLocation;
    private HubMetadataConfiguration hubMetadataConfiguration;

    public VerifyHubConfiguration(HubEnvironment hubEnvironment) {
        this(hubEnvironment, null, null);
    }

    @JsonCreator
    public VerifyHubConfiguration(
            @JsonProperty("environment") HubEnvironment hubEnvironment,
            @JsonProperty("ssoLocation") URI hubSsoLocation,
            @JsonProperty("metadata") HubMetadataConfiguration hubMetadataConfiguration
    ) {
        this.hubEnvironment = hubEnvironment;

        this.hubSsoLocation = ofNullable(hubSsoLocation).orElseGet(() ->
                ofNullable(hubEnvironment)
                        .map(HubEnvironment::getSsoLocation)
                        .orElseThrow(() -> new IllegalArgumentException("Missing configuration value for ssoLocation. Either provide an override or set environment in the verifyHubConfiguration section")));

        this.hubMetadataConfiguration = ofNullable(hubMetadataConfiguration).orElse(createHubMetadataConfigurationWithDefaults());
        this.hubMetadataConfiguration.setEnvironment(hubEnvironment);
    }

    public HubEnvironment getHubEnvironment() {
        return hubEnvironment;
    }

    public URI getHubSsoLocation() {
        return hubSsoLocation;
    }

    public HubMetadataConfiguration getHubMetadataConfiguration() {
        return hubMetadataConfiguration;
    }

    private HubMetadataConfiguration createHubMetadataConfigurationWithDefaults() {
        return new HubMetadataConfiguration(null, null, null, null, null, null, null, null, null, null);
    }
}
