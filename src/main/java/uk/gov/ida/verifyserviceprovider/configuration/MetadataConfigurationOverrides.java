package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;

import java.net.URI;

public class MetadataConfigurationOverrides {
    private String trustStorePath;
    private String trustStorePassword;
    private URI uri;
    private Long minRefreshDelay;
    private Long maxRefreshDelay;
    private String expectedEntityId;
    private JerseyClientConfiguration jerseyClientConfiguration;
    private String jerseyClientName;

    @JsonCreator
    public MetadataConfigurationOverrides(
        @JsonProperty("trustStorePath") String trustStorePath,
        @JsonProperty("trustStorePassword") String trustStorePassword,
        @JsonProperty("uri") URI uri,
        @JsonProperty("minRefreshDelay") Long minRefreshDelay,
        @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
        @JsonProperty("expectedEntityId") String expectedEntityId,
        @JsonProperty("jerseyClientConfiguration") JerseyClientConfiguration jerseyClientConfiguration,
        @JsonProperty("jerseyClientName") String jerseyClientName
    ) {
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.uri = uri;
        this.minRefreshDelay = minRefreshDelay;
        this.maxRefreshDelay = maxRefreshDelay;
        this.expectedEntityId = expectedEntityId;
        this.jerseyClientConfiguration = jerseyClientConfiguration;
        this.jerseyClientName = jerseyClientName;
    }

    public MetadataConfigurationOverrides() {}

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public URI getUri() {
        return uri;
    }

    public Long getMinRefreshDelay() {
        return minRefreshDelay;
    }

    public Long getMaxRefreshDelay() {
        return maxRefreshDelay;
    }

    public String getExpectedEntityId() {
        return expectedEntityId;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }

    public String getJerseyClientName() {
        return jerseyClientName;
    }
}
