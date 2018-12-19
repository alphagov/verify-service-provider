package uk.gov.ida.verifyserviceprovider.configuration;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import httpstub.HttpStubRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import keystore.KeyStoreResource;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.opensaml.xmlsec.algorithm.descriptors.DigestMD5;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.EntitiesDescriptorFactory;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import static io.dropwizard.testing.ConfigOverride.config;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_EXPECTED_ENTITY_ID;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_METADATA_URL;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_SSO_URL;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.IDP_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.METADATA_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.TRUSTSTORE_PASSWORD;

public class HubMetadataFeatureTest {

    private final String HEALTHCHECK_URL = "http://localhost:%d/admin/healthcheck";

    @ClassRule
    public static HttpStubRule hubServer = new HttpStubRule();

    @ClassRule
    public static MockMsaServer msaServer = new MockMsaServer();

    @ClassRule
    public static EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private DropwizardTestSupport<VerifyServiceProviderConfiguration> applicationTestSupport;

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        msaServer.serveDefaultMetadata();
        KeyStoreResource verifyMetadataKeystoreResource = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();
        verifyMetadataKeystoreResource.create();
        KeyStoreResource verifyHubKeystoreResource = aKeyStoreResource()
                .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(CACertificates.TEST_CORE_CA).build().getCertificate())
                .build();
        verifyHubKeystoreResource.create();
        applicationTestSupport = new DropwizardTestSupport<>(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            config("server.connector.port", "0"),
            config("verifyHubConfiguration.environment", "CUSTOM"),
            config("msaMetadata.uri", msaServer::getUri),
            config("msaMetadata.expectedEntityId", MockMsaServer.MSA_ENTITY_ID),
            config("serviceEntityIds", "[\"http://some-service-entity-id\"]"),
            config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY)
        );

        new HashMap<String, String>() {{
            put(HUB_SSO_URL, String.format("http://localhost:%s/SAML2/SSO", hubServer.getPort()));
            put(HUB_METADATA_URL, getHubMetadataUrl());
            put(HUB_EXPECTED_ENTITY_ID, HUB_ENTITY_ID);
            put(METADATA_TRUSTSTORE_PATH, verifyMetadataKeystoreResource.getAbsolutePath());
            put(HUB_TRUSTSTORE_PATH, verifyHubKeystoreResource.getAbsolutePath());
            put(IDP_TRUSTSTORE_PATH, verifyMetadataKeystoreResource.getAbsolutePath());
            put(TRUSTSTORE_PASSWORD, verifyMetadataKeystoreResource.getPassword());
        }}.forEach(environmentVariables::set);
    }

    private String getHubMetadataUrl() {
        return format("http://localhost:%s/SAML2/metadata", hubServer.getPort());
    }

    @After
    public void tearDown() {
        applicationTestSupport.after();
    }

    @Test
    public void shouldFailHealthcheckWhenHubMetadataUnavailable() {
        hubServer.register("/SAML2/metadata", 500);

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = format("\"%s\":{\"healthy\":false", getHubMetadataUrl());

        assertThat(hubServer.getRecordedRequest()).anyMatch(rr -> rr.getPath().equals("/SAML2/metadata"));

        assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }

    @Test
    public void shouldFailHealthcheckWhenHubMetadataIsSignedWithMD5() throws JsonProcessingException {
        String id = UUID.randomUUID().toString();
        Signature signature = SignatureBuilder.aSignature()
            .withDigestAlgorithm(id, new DigestMD5())
            .withX509Data(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT)
            .withSigningCredential(new TestCredentialFactory(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT,
                    TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential()).build();
        String metadata = new MetadataFactory().metadata(new EntitiesDescriptorFactory().signedEntitiesDescriptor(id, signature));

        hubServer.register("/SAML2/metadata", 200, metadata);

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = format("\"%s\":{\"healthy\":false", getHubMetadataUrl());

        assertThat(hubServer.getRecordedRequest()).anyMatch(rr -> rr.getPath().equals("/SAML2/metadata"));

        assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }

    @Test
    public void shouldPassHealthcheckWhenHubMetadataAvailable() throws JsonProcessingException {
        hubServer.register("/SAML2/metadata", 200, "application/samlmetadata+xml", new MetadataFactory().defaultMetadata());

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = format("\"%s\":{\"healthy\":true", getHubMetadataUrl());

        assertThat(hubServer.getRecordedRequest()).anyMatch(rr -> rr.getPath().equals("/SAML2/metadata"));

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }
}
