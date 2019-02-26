package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.KeyStore;
import java.util.Optional;

public class EuropeanIdentityConfiguration extends EidasMetadataConfiguration {

    private TrustStoreConfiguration trustStoreConfiguration;
    private String hubConnectorEntityId;
    private boolean enabled;
    private HubEnvironment environment;


    @JsonCreator
    public EuropeanIdentityConfiguration(@JsonProperty("hubConnectorEntityId") String hubConnectorEntityId,
                                         @NotNull @Valid @JsonProperty("enabled") boolean enabled,
                                         @JsonProperty("trustAnchorUri") URI trustAnchorUri,
                                         @JsonProperty("minRefreshDelay") Long minRefreshDelay,
                                         @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
                                         @JsonProperty("trustAnchorMaxRefreshDelay") Long trustAnchorMaxRefreshDelay,
                                         @JsonProperty("trustAnchorMinRefreshDelay") Long trustAnchorMinRefreshDelay,
                                         @JsonProperty("client") JerseyClientConfiguration client,
                                         @JsonProperty("jerseyClientName") String jerseyClientName,
                                         @JsonProperty("trustStore") TrustStoreConfiguration trustStore,
                                         @JsonProperty("metadataSourceUri") URI metadataSourceUri
    ){
        super(trustAnchorUri, minRefreshDelay, maxRefreshDelay, trustAnchorMaxRefreshDelay, trustAnchorMinRefreshDelay, client, jerseyClientName, trustStore, metadataSourceUri);
        this.enabled = enabled;
        this.hubConnectorEntityId = hubConnectorEntityId;
        this.trustStoreConfiguration = trustStore;
    }

    @JsonIgnore
    public void setEnvironment(HubEnvironment environment) {
        this.environment = environment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getHubConnectorEntityId() {
        return Optional.ofNullable(hubConnectorEntityId)
                .orElse(environment.getEidasHubConnectorEntityId());
    }

    @Override
    public KeyStore getTrustStore() {
        if (trustStoreConfiguration !=null){
            return super.getTrustStore();
        }
        return environment.getMetadataTrustStore();
    }
    @Override
    public URI getMetadataSourceUri() {
        return Optional.ofNullable(super.getMetadataSourceUri())
                .orElse(environment.getEidasMetadataSourceUri());
    }

    @Override
    public URI getTrustAnchorUri() {
        return Optional.ofNullable(super.getTrustAnchorUri())
                .orElse(environment.getEidasMetadataTrustAnchorUri());
    }

}
