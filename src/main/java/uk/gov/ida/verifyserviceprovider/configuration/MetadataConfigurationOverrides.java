package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;

import java.net.URI;

public class MetadataConfigurationOverrides {
    private String trustStorePath;
    private String trustStorePassword;
    private URI metadataUri;
    private Long minRefreshDelay;
    private Long maxRefreshDelay;
    private String entityId;
    private JerseyClientConfiguration jerseyClientConfiguration;
    private String jerseyClientName;

    @JsonCreator
    public MetadataConfigurationOverrides(
        @JsonProperty("trustStorePath") String trustStorePath,
        @JsonProperty("trustStorePassword") String trustStorePassword,
        @JsonProperty("metadataUri") URI metadataUri,
        @JsonProperty("minRefreshDelay") Long minRefreshDelay,
        @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
        @JsonProperty("entityId") String entityId,
        @JsonProperty("jerseyClientConfiguration") JerseyClientConfiguration jerseyClientConfiguration,
        @JsonProperty("jerseyClientName") String jerseyClientName
    ) {
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.metadataUri = metadataUri;
        this.minRefreshDelay = minRefreshDelay;
        this.maxRefreshDelay = maxRefreshDelay;
        this.entityId = entityId;
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

    public URI getMetadataUri() {
        return metadataUri;
    }

    public Long getMinRefreshDelay() {
        return minRefreshDelay;
    }

    public Long getMaxRefreshDelay() {
        return maxRefreshDelay;
    }

    public String getEntityId() {
        return entityId;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }

    public String getJerseyClientName() {
        return jerseyClientName;
    }
}
