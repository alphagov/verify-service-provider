package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Throwables;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.servlets.assets.ResourceNotFoundException;
import uk.gov.ida.saml.metadata.EncodedTrustStoreConfiguration;
import uk.gov.ida.saml.metadata.KeyStoreLoader;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;
import uk.gov.ida.saml.metadata.exception.EmptyTrustStoreException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;

import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_VERIFY_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_VERIFY_TRUSTSTORE_NAME;

public class HubMetadataConfiguration extends MetadataConfiguration {

    private final TrustStoreConfiguration trustStoreConfiguration;

    @JsonCreator
    public HubMetadataConfiguration(
            @JsonProperty("uri") @JsonAlias({"url"}) URI uri,
            @JsonProperty("minRefreshDelay") Long minRefreshDelay,
            @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
            @JsonProperty("expectedEntityId") String expectedEntityId,
            @JsonProperty("client") JerseyClientConfiguration client,
            @JsonProperty("jerseyClientName") String jerseyClientName,
            @JsonProperty("hubFederationId") String hubFederationId,
            @JsonProperty("trustStore") TrustStoreConfiguration trustStoreConfiguration) {
        super(uri, minRefreshDelay, maxRefreshDelay, expectedEntityId, client,
                ofNullable(jerseyClientName).orElse(HUB_JERSEY_CLIENT_NAME), hubFederationId);
        this.trustStoreConfiguration = trustStoreConfiguration;
    }

    public HubMetadataConfiguration(URI uri, String expectedEntityId, TrustStoreConfiguration trustStoreConfiguration) {
        this(uri, null, null, expectedEntityId, null, null, null, trustStoreConfiguration);
    }

    @Override
    public KeyStore getTrustStore() {
        return trustStoreConfiguration.getTrustStore();
    }
}
