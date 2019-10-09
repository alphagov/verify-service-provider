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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EuropeanIdentityConfiguration extends EidasMetadataConfiguration {

    private TrustStoreConfiguration trustStoreConfiguration;
    private String hubConnectorEntityId;
    private List<String> acceptableHubConnectorEntityIds;
    private boolean enabled;
    private HubEnvironment environment;


    @JsonCreator
    public EuropeanIdentityConfiguration(@JsonProperty("hubConnectorEntityId") String hubConnectorEntityId,
                                         @JsonProperty("acceptableHubConnectorEntityIds") List<String> acceptableHubConnectorEntityIds,
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
        this.trustStoreConfiguration = trustStore;
        this.hubConnectorEntityId = hubConnectorEntityId;
        this.acceptableHubConnectorEntityIds = acceptableHubConnectorEntityIds;
    }

    @JsonIgnore
    public void setEnvironment(HubEnvironment environment) {
        this.environment = environment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getAllAcceptableHubConnectorEntityIds() {
        Set<String> entityIds = new HashSet<>(environment.getEidasDefaultAcceptableHubConnectorEntityIds());
        Optional.ofNullable(hubConnectorEntityId).ifPresent(entityIds::add);
        Optional.ofNullable(acceptableHubConnectorEntityIds).ifPresent(entityIds::addAll);
        return new ArrayList<>(entityIds);
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
