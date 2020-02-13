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
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;

public class HubMetadataConfiguration extends MetadataConfiguration {

    private HubEnvironment environment;
    private final TrustStoreConfiguration trustStoreConfiguration;
    private final TrustStoreConfiguration hubTrustStoreConfiguration;
    private final TrustStoreConfiguration idpTrustStoreConfiguration;

    @JsonCreator
    public HubMetadataConfiguration(
            @JsonProperty("uri") @JsonAlias({"url"}) URI uri,
            @JsonProperty("minRefreshDelay") Long minRefreshDelay,
            @JsonProperty("maxRefreshDelay") Long maxRefreshDelay,
            @JsonProperty("expectedEntityId") String expectedEntityId,
            @JsonProperty("client") JerseyClientConfiguration client,
            @JsonProperty("jerseyClientName") String jerseyClientName,
            @JsonProperty("hubFederationId") String hubFederationId,
            @JsonProperty("trustStore") TrustStoreConfiguration trustStoreConfiguration,
            @JsonProperty("hubTrustStore") TrustStoreConfiguration hubTrustStoreConfiguration,
            @JsonProperty("idpTrustStore") TrustStoreConfiguration idpTrustStoreConfiguration) {
        super(uri, minRefreshDelay, maxRefreshDelay, expectedEntityId, client,
                ofNullable(jerseyClientName).orElse(HUB_JERSEY_CLIENT_NAME), hubFederationId);
        this.trustStoreConfiguration = trustStoreConfiguration;
        this.hubTrustStoreConfiguration = hubTrustStoreConfiguration;
        this.idpTrustStoreConfiguration = idpTrustStoreConfiguration;
    }

    public void setEnvironment(HubEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public URI getUri() {
        return ofNullable(super.getUri()).orElseGet(() ->
                getValueFromEnvironment(HubEnvironment::getMetadataUri, "metadata.uri"));
    }

    @Override
    public String getExpectedEntityId() {
        return ofNullable(super.getExpectedEntityId()).orElseGet(() -> generateExpectedEntityId(environment));
    }

    @Override
    public KeyStore getTrustStore() {
        return ofNullable(trustStoreConfiguration)
                .map(this::tryGetTrustStore)
                .map(this::validateTruststore)
                .orElseGet(() -> getValueFromEnvironment(HubEnvironment::getMetadataTrustStore, "metadata.trustStore"));
    }

    @Override
    public Optional<KeyStore> getHubTrustStore() {
        return of(ofNullable(hubTrustStoreConfiguration)
                .map(this::tryGetTrustStore)
                .map(this::validateTruststore)
                .orElseGet(() -> getValueFromEnvironment(HubEnvironment::getHubTrustStore, "metadata.hubTrustStore")));
    }

    @Override
    public Optional<KeyStore> getIdpTrustStore() {
        return of(ofNullable(idpTrustStoreConfiguration)
                .map(this::tryGetTrustStore)
                .map(this::validateTruststore)
                .orElseGet(() -> getValueFromEnvironment(HubEnvironment::getIdpTrustStore, "metadata.idpTrustStore")));
    }

    private static String generateExpectedEntityId(HubEnvironment hubEnvironment) {
        String expectedEntityId;
        switch (hubEnvironment) {
            case PRODUCTION:
            case INTEGRATION:
            case COMPLIANCE_TOOL:
                expectedEntityId = "https://signin.service.gov.uk";
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

    private KeyStore tryGetTrustStore(TrustStoreConfiguration trustStoreConfiguration) {
        try {
            return trustStoreConfiguration.getTrustStore();
        } catch (NullPointerException ignored) {
            return null;
        }
    }

    private <T> T getValueFromEnvironment(Function<HubEnvironment, T> supplier, String valueName) {
        return ofNullable(this.environment)
                .map(supplier)
                .orElseThrow(() -> createConfigValueMissingException(valueName));
    }

    private RuntimeException createConfigValueMissingException(String valueName) {
        return new IllegalArgumentException("Missing configuration value for " + valueName + ". Either provide an override or set environment in the verifyHubConfiguration section");
    }
}
