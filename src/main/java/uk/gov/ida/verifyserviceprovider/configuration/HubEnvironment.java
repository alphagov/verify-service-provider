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
import java.util.Optional;

import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.DEFAULT_TRUST_STORE_PASSWORD;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_EXPECTED_ENTITY_ID;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_METADATA_URL;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_SSO_URL;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.IDP_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.METADATA_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.TRUSTSTORE_PASSWORD;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PROD_HUB_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PROD_IDP_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PROD_METADATA_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_HUB_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_IDP_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_METADATA_TRUSTSTORE_NAME;

public enum HubEnvironment implements MetadataResolverConfiguration {
    PRODUCTION(
            "https://www.signin.service.gov.uk/SAML2/SSO",
            "https://www.signin.service.gov.uk/SAML2/metadata/federation",
            "https://signin.service.gov.uk",
            PROD_METADATA_TRUSTSTORE_NAME,
            PROD_HUB_TRUSTSTORE_NAME,
            PROD_IDP_TRUSTSTORE_NAME,
            DEFAULT_TRUST_STORE_PASSWORD,
            true
    ),
    INTEGRATION(
            "https://www.integration.signin.service.gov.uk/SAML2/SSO",
            "https://www.integration.signin.service.gov.uk/SAML2/metadata/federation",
            "https://signin.service.gov.uk",
            TEST_METADATA_TRUSTSTORE_NAME,
            TEST_HUB_TRUSTSTORE_NAME,
            TEST_IDP_TRUSTSTORE_NAME,
            DEFAULT_TRUST_STORE_PASSWORD,
            true
    ),
    COMPLIANCE_TOOL(
            "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/SSO",
            "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation",
            "https://signin.service.gov.uk",
            TEST_METADATA_TRUSTSTORE_NAME,
            TEST_HUB_TRUSTSTORE_NAME,
            TEST_IDP_TRUSTSTORE_NAME,
            DEFAULT_TRUST_STORE_PASSWORD,
            true
    ),
    CUSTOM(
            System.getenv().getOrDefault(HUB_SSO_URL, ""),
            System.getenv().getOrDefault(HUB_METADATA_URL, ""),
            System.getenv().getOrDefault(HUB_EXPECTED_ENTITY_ID, ""),
            System.getenv().getOrDefault(METADATA_TRUSTSTORE_PATH, ""),
            System.getenv().getOrDefault(HUB_TRUSTSTORE_PATH, ""),
            System.getenv().getOrDefault(IDP_TRUSTSTORE_PATH, ""),
            System.getenv().getOrDefault(TRUSTSTORE_PASSWORD, ""),
            false
    );

    private String ssoLocation;
    private String metadataUri;
    private String expectedEntityId;
    private String metadataTrustStorePath;
    private String hubTrustStorePath;
    private String idpTrustStorePath;
    private String trustStorePassword;
    private boolean loadTruststoreFromResources;

    @JsonCreator
    public static HubEnvironment fromString(String name) {
        return Arrays.stream(values())
            .filter(x -> name.equals(x.name()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Unrecognised Hub Environment: " + name + ". \n" +
                "Valid values are: PRODUCTION, INTEGRATION, COMPLIANCE_TOOL, CUSTOM"
            ));
    }

    HubEnvironment(String ssoLocation,
                   String metadataUri,
                   String expectedEntityId,
                   String metadataTrustStorePath,
                   String hubTrustStorePath,
                   String idpTrustStorePath,
                   String trustStorePassword,
                   boolean loadTruststoreFromResources) {
        this.ssoLocation = ssoLocation;
        this.metadataUri = metadataUri;
        this.expectedEntityId = expectedEntityId;
        this.metadataTrustStorePath = metadataTrustStorePath;
        this.hubTrustStorePath = hubTrustStorePath;
        this.idpTrustStorePath = idpTrustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.loadTruststoreFromResources = loadTruststoreFromResources;
    }

    public URI getSsoLocation() {
        return URI.create(this.ssoLocation);
    }

    @Override
    public KeyStore getTrustStore() {
        return loadTruststore(metadataTrustStorePath);
    }

    @Override
    public Optional<KeyStore> getHubTrustStore() {
        return Optional.ofNullable(loadTruststore(hubTrustStorePath));
    }

    @Override
    public Optional<KeyStore> getIdpTrustStore() {
        return Optional.ofNullable(loadTruststore(idpTrustStorePath));
    }

    @Override
    public URI getUri() {
        return URI.create(metadataUri);
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
            return new KeyStoreLoader().load(new FileInputStream(trustStore), trustStorePassword);
        } catch (FileNotFoundException e) {
            throw new TrustStoreLoadingException(trustStore);
        }
    }

    private KeyStore loadTruststoreFromResources(String trustStore) {
        InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream(trustStore);
        if (trustStoreStream == null) {
            throw new TrustStoreLoadingException(trustStore);
        }
        return new KeyStoreLoader().load(trustStoreStream, trustStorePassword);
    }
}
