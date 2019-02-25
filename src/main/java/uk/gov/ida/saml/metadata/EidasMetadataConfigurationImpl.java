package uk.gov.ida.saml.metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.HubEnvironment;

import java.net.URI;
import java.security.KeyStore;
import java.util.Optional;

public class EidasMetadataConfigurationImpl extends EidasMetadataConfiguration {

    private TrustStoreConfiguration trustStoreConfiguration;
    private HubEnvironment environment;

    public EidasMetadataConfigurationImpl(){
        this(null, null, null, null, null, null, null, null, null);
    }
    @JsonCreator
    public EidasMetadataConfigurationImpl(@JsonProperty("trustAnchorUri") URI trustAnchorUri,
                                      @JsonProperty("minRefreshDelay") Long minRefreshDelay,
                                      @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
                                      @JsonProperty("trustAnchorMaxRefreshDelay") Long trustAnchorMaxRefreshDelay,
                                      @JsonProperty("trustAnchorMinRefreshDelay") Long trustAnchorMinRefreshDelay,
                                      @JsonProperty("client") JerseyClientConfiguration client,
                                      @JsonProperty("jerseyClientName") String jerseyClientName,
                                      @JsonProperty("trustStore") TrustStoreConfiguration trustStore,
                                      @JsonProperty("metadataSourceUri") URI metadataSourceUri
    )
    {
        super(trustAnchorUri, minRefreshDelay, maxRefreshDelay, trustAnchorMaxRefreshDelay, trustAnchorMinRefreshDelay, client, jerseyClientName, trustStore, metadataSourceUri);
        this.trustStoreConfiguration = trustStore;
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

    public void setEnvironment(HubEnvironment environment) {
        this.environment = environment;
    }
}
