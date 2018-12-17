package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;

import java.net.URI;

public class VerifyHubConfiguration {

    private final HubEnvironment hubEnvironment;

    @JsonCreator
    public VerifyHubConfiguration(
        @JsonProperty("environment") HubEnvironment hubEnvironment
    ) {
        this.hubEnvironment = hubEnvironment;
    }

    public URI getHubSsoLocation() {
        return hubEnvironment.getSsoLocation();
    }

    public MetadataResolverConfiguration getHubMetadataConfiguration() {
        return hubEnvironment;
    }
}
