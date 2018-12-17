package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.saml.metadata.KeyStoreLoader;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;
import uk.gov.ida.verifyserviceprovider.exceptions.TrustStoreLoadingException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.Arrays;

import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.DEFAULT_TRUST_STORE_PASSWORD;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PROD_METADATA_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_METADATA_TRUSTSTORE_NAME;

public enum HubEnvironment implements MetadataResolverConfiguration {
    PRODUCTION(
            URI.create("https://www.signin.service.gov.uk/SAML2/SSO"),
            URI.create("https://www.signin.service.gov.uk/SAML2/metadata/federation"),
            "https://signin.service.gov.uk",
            PROD_METADATA_TRUSTSTORE_NAME,
            true),
    INTEGRATION(
            URI.create("https://www.integration.signin.service.gov.uk/SAML2/SSO"),
            URI.create("https://www.integration.signin.service.gov.uk/SAML2/metadata/federation"),
            "https://signin.service.gov.uk",
            TEST_METADATA_TRUSTSTORE_NAME,
            true),
    COMPLIANCE_TOOL(
            URI.create("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/SSO"),
            URI.create("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation"),
            "https://signin.service.gov.uk",
            TEST_METADATA_TRUSTSTORE_NAME,
            true);

    private URI ssoLocation;
    private URI metadataUri;
    private String expectedEntityId;
    private String metadataTrustStorePath;
    private boolean loadTruststoreFromResources;

    @JsonCreator
    public static HubEnvironment fromString(String name) {
        return Arrays.stream(values())
            .filter(x -> name.equals(x.name()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Unrecognised Hub Environment: " + name + ". \n" +
                "Valid values are: PRODUCTION, INTEGRATION, COMPLIANCE_TOOL"
            ));
    }

    HubEnvironment(URI ssoLocation,
                   URI metadataUri,
                   String expectedEntityId,
                   String metadataTrustStorePath,
                   boolean loadTruststoreFromResources) {
        this.ssoLocation = ssoLocation;
        this.metadataUri = metadataUri;
        this.expectedEntityId = expectedEntityId;
        this.metadataTrustStorePath = metadataTrustStorePath;
        this.loadTruststoreFromResources = loadTruststoreFromResources;
    }

    public URI getSsoLocation() {
        return this.ssoLocation;
    }

    @Override
    public KeyStore getTrustStore() {
        return loadTruststore(metadataTrustStorePath);
    }

    @Override
    public URI getUri() {
        return metadataUri;
    }

    @Override
    public Long getMinRefreshDelay() {
        return 60000L;
    }

    @Override
    public Long getMaxRefreshDelay() {
        return 600000L;
    }

    @Override
    public String getExpectedEntityId() {
        return expectedEntityId;
    }

    @Override
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return new JerseyClientConfiguration();
    }

    @Override
    public String getJerseyClientName() {
        return HUB_JERSEY_CLIENT_NAME;
    }

    @Override
    public String getHubFederationId() {
        return "VERIFY-FEDERATION";
    }

    private KeyStore loadTruststore(String trustStore) {
        return loadTruststoreFromResources ? loadTruststoreFromResources(trustStore) : loadTruststoreFromFile(trustStore);
    }

    private KeyStore loadTruststoreFromFile(String trustStore) {
        try {
            return new KeyStoreLoader().load(new FileInputStream(trustStore), DEFAULT_TRUST_STORE_PASSWORD);
        } catch (FileNotFoundException e) {
            throw new TrustStoreLoadingException(trustStore);
        }
    }

    private KeyStore loadTruststoreFromResources(String trustStore) {
        InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream(trustStore);
        if (trustStoreStream == null) {
            throw new TrustStoreLoadingException(trustStore);
        }
        return new KeyStoreLoader().load(trustStoreStream, DEFAULT_TRUST_STORE_PASSWORD);
    }
}
