package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;
import uk.gov.ida.saml.metadata.exception.EmptyTrustStoreException;

import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;

import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.MSA_JERSEY_CLIENT_NAME;

public class MsaMetadataConfiguration extends MetadataConfiguration {

    private final TrustStoreConfiguration trustStoreConfiguration;

    @JsonCreator
    public MsaMetadataConfiguration(
            @JsonProperty("uri") @JsonAlias({ "url" }) URI uri,
            @JsonProperty("minRefreshDelay") Long minRefreshDelay,
            @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
            @JsonProperty(value = "expectedEntityId", required = true) String expectedEntityId,
            @JsonProperty("client") JerseyClientConfiguration client,
            @JsonProperty("jerseyClientName") String jerseyClientName,
            @JsonProperty("hubFederationId") String hubFederationId,
            @JsonProperty("trustStore") TrustStoreConfiguration trustStoreConfiguration
    ) {
        super(uri, minRefreshDelay, maxRefreshDelay, expectedEntityId, client, ofNullable(jerseyClientName).orElse(MSA_JERSEY_CLIENT_NAME), hubFederationId);
        this.trustStoreConfiguration = trustStoreConfiguration;
    }

    @Override
    public KeyStore getTrustStore() {
        return validateTruststore(trustStoreConfiguration.getTrustStore());
    }

    private KeyStore validateTruststore(KeyStore trustStore) {
        int trustStoreSize;
        try {
            trustStoreSize = trustStore.size();
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        if (trustStoreSize == 0) {
            throw new EmptyTrustStoreException();
        }
        return trustStore;
    }
}
