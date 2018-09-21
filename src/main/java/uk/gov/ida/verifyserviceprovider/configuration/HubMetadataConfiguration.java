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
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;

public class HubMetadataConfiguration extends MetadataConfiguration {

    private HubEnvironment environment;
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

    public void setEnvironment(HubEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public URI getUri() {
        return ofNullable(super.getUri()).orElse(environment.getMetadataUri());
    }

    @Override
    public String getExpectedEntityId() {
        return ofNullable(super.getExpectedEntityId()).orElseGet(() -> generateExpectedEntityId(environment));
    }

    @Override
    public KeyStore getTrustStore() {
        return validateTruststore(ofNullable(trustStoreConfiguration)
                .orElseGet(() -> new DefaultHubTrustStoreConfiguration(environment)).getTrustStore());
    }

    private static String generateExpectedEntityId(HubEnvironment hubEnvironment) {
        String expectedEntityId;
        switch (hubEnvironment) {
            case PRODUCTION:
            case INTEGRATION:
            case COMPLIANCE_TOOL:
                expectedEntityId = "https://signin.service.gov.uk";
                break;
            case COMPLIANCE_TOOL_LOCAL:
                expectedEntityId = "https://local.signin.service.gov.uk";
                break;
            default:
                throw new RuntimeException("No entity ID configured for Hub Environment: " + hubEnvironment.name());
        }

        return expectedEntityId;
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
