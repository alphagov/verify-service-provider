package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;
import uk.gov.ida.saml.metadata.EidasMetadataConfigurationImpl;
import uk.gov.ida.saml.metadata.EncodedTrustStoreConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.KeyStore;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class EuropeanIdentityConfiguration {

    private String hubConnectorEntityId;
    private boolean enabled;
    private EidasMetadataConfigurationImpl aggregatedMetadata;


    @JsonCreator
    public EuropeanIdentityConfiguration(
            @JsonProperty("hubConnectorEntityId") String hubConnectorEntityId,
            @NotNull @Valid @JsonProperty("enabled") boolean enabled,
            @Valid @JsonProperty("aggregatedMetadata") EidasMetadataConfigurationImpl aggregatedMetadata
    ){
        this.enabled = enabled;
        this.hubConnectorEntityId = hubConnectorEntityId;
        this.aggregatedMetadata = ofNullable(aggregatedMetadata).orElse(new EidasMetadataConfigurationImpl());
    }

    public String getHubConnectorEntityId() {
        return hubConnectorEntityId;
    }

    @JsonIgnore
    public void setEnvironment(HubEnvironment environment) {
        this.aggregatedMetadata.setEnvironment(environment);
    }

    public EidasMetadataConfiguration getAggregatedMetadata() {
        return aggregatedMetadata;
    }

    public URI getMetadataSourceUri() {
        return aggregatedMetadata.getMetadataSourceUri();
    }

    public KeyStore getTrustStore() {
        return aggregatedMetadata.getTrustStore();
    }

    public URI getTrustAnchorUri() {
      return aggregatedMetadata.getTrustAnchorUri();

    }

    public boolean isEnabled() {
        return enabled;
    }

}
