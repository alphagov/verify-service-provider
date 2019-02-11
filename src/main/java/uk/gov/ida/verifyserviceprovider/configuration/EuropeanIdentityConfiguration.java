package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;
import uk.gov.ida.saml.metadata.EncodedTrustStoreConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.KeyStore;

import static java.util.Optional.ofNullable;

public class EuropeanIdentityConfiguration {

    private String hubConnectorEntityId;
    private boolean enabled;
    private EidasMetadataConfiguration aggregatedMetadata;
    private HubEnvironment environment;

    @JsonCreator
    public EuropeanIdentityConfiguration(
            @JsonProperty("hubConnectorEntityId") String hubConnectorEntityId,
            @NotNull @Valid @JsonProperty("enabled") boolean enabled,
            @Valid @JsonProperty("aggregatedMetadata") EidasMetadataConfiguration aggregatedMetadata
    ){
        this.enabled = enabled;
        this.hubConnectorEntityId = hubConnectorEntityId;
        this.aggregatedMetadata = ofNullable(aggregatedMetadata).orElse(createEidasMetadataConfigurationWithDefaults());
    }

    public String getHubConnectorEntityId() {
        return hubConnectorEntityId;
    }

    @JsonIgnore
    public void setEnvironment(HubEnvironment environment) {
        this.environment = environment;
    }

    public EidasMetadataConfiguration getAggregatedMetadata() {
        return aggregatedMetadata;
    }

    public URI getMetadataSourceUri() {
        return ofNullable(aggregatedMetadata)
                .map(EidasMetadataConfiguration::getMetadataSourceUri)
                .orElseGet(environment::getEidasMetadataSourceUri);
    }

    public KeyStore getTrustStore() {
        return ofNullable(aggregatedMetadata)
                .map(EidasMetadataConfiguration::getTrustStore)
                .orElseGet(environment::getMetadataTrustStore);
    }

    public URI getTrustAnchorUri() {
        return ofNullable(aggregatedMetadata)
                .map(EidasMetadataConfiguration::getTrustAnchorUri)
                .orElseGet(environment::getEidasMetadataTrustAnchorUri);
    }

    public boolean isEnabled() {
        return enabled;
    }

    private EidasMetadataConfiguration createEidasMetadataConfigurationWithDefaults() {
        return new EidasMetadataConfiguration(null, 0L, 0L, 0L, 0L, null, null, new EncodedTrustStoreConfiguration(), null);
    }
}
