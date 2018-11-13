package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.dropwizard.servlets.assets.ResourceNotFoundException;
import uk.gov.ida.saml.metadata.KeyStoreLoader;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.Arrays;

import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.DEFAULT_TRUST_STORE_PASSWORD;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_VERIFY_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_VERIFY_TRUSTSTORE_NAME;

public enum HubEnvironment {
    PRODUCTION(
        URI.create("https://www.signin.service.gov.uk/SAML2/SSO"),
        URI.create("https://www.signin.service.gov.uk/SAML2/metadata/federation"),
        "https://signin.service.gov.uk",
        PRODUCTION_VERIFY_TRUSTSTORE_NAME
    ),
    INTEGRATION(
        URI.create("https://www.integration.signin.service.gov.uk/SAML2/SSO"),
        URI.create("https://www.integration.signin.service.gov.uk/SAML2/metadata/federation"),
        "https://signin.service.gov.uk",
        TEST_VERIFY_TRUSTSTORE_NAME
    ),
    COMPLIANCE_TOOL(
        URI.create("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/SSO"),
        URI.create("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation"),
        "https://signin.service.gov.uk",
        TEST_VERIFY_TRUSTSTORE_NAME
    );

    private URI ssoLocation;
    private URI metadataUri;
    private String expectedEntityId;
    private String trustStoreName;

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

    HubEnvironment(URI ssoLocation, URI metadataUri, String expectedEntityId, String trustStoreName) {
        this.ssoLocation = ssoLocation;
        this.metadataUri = metadataUri;
        this.expectedEntityId = expectedEntityId;
        this.trustStoreName = trustStoreName;
    }

    public URI getSsoLocation() {
        return this.ssoLocation;
    }

    public HubMetadataConfiguration getMetadataConfiguration() {
        return new HubMetadataConfiguration(this.metadataUri, this.expectedEntityId, this.getTrustStoreConfiguration());
    }

    private TrustStoreConfiguration getTrustStoreConfiguration() {
        return new TrustStoreConfiguration() {
            @Override
            public KeyStore getTrustStore() {
                InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream(trustStoreName);
                if (trustStoreStream == null) {
                    throw new ResourceNotFoundException(new FileNotFoundException("Could not load resource from path " + trustStoreName));
                }
                return new KeyStoreLoader().load(trustStoreStream, DEFAULT_TRUST_STORE_PASSWORD);
            }
        };
    }
}
