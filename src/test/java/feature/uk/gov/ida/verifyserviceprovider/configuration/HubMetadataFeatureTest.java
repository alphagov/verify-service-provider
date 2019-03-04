package feature.uk.gov.ida.verifyserviceprovider.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import keystore.KeyStoreResource;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.xmlsec.algorithm.descriptors.DigestMD5;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.EntitiesDescriptorFactory;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import static certificates.values.CACertificates.TEST_CORE_CA;
import static certificates.values.CACertificates.TEST_IDP_CA;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
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

public class HubMetadataFeatureTest {

    private final String HEALTHCHECK_URL = "http://localhost:%d/admin/healthcheck";

    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    @ClassRule
    public static MockMsaServer msaServer = new MockMsaServer();
    private DropwizardTestSupport<VerifyServiceProviderConfiguration> applicationTestSupport;

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        wireMockServer.start();
        msaServer.serveDefaultMetadata();

        KeyStoreResource metadataTrustStore = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();
        KeyStoreResource hubTrustStore = aKeyStoreResource()
                .withCertificate("VERIFY-HUB", aCertificate().withCertificate(TEST_CORE_CA).build().getCertificate())
                .build();
        KeyStoreResource idpTrustStore = aKeyStoreResource()
                .withCertificate("VERIFY-IDP", aCertificate().withCertificate(TEST_IDP_CA).build().getCertificate())
                .build();

        metadataTrustStore.create();
        hubTrustStore.create();
        idpTrustStore.create();

        applicationTestSupport = new DropwizardTestSupport<>(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            config("server.connector.port", "0"),
            config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            config("verifyHubConfiguration.metadata.uri", getHubMetadataUrl()),
            config("verifyHubConfiguration.metadata.expectedEntityId", HUB_ENTITY_ID),
            config("verifyHubConfiguration.metadata.trustStore.path", metadataTrustStore.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.trustStore.password", metadataTrustStore.getPassword()),
            config("verifyHubConfiguration.metadata.hubTrustStore.path", hubTrustStore.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.hubTrustStore.password", hubTrustStore.getPassword()),
            config("verifyHubConfiguration.metadata.idpTrustStore.path", idpTrustStore.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.idpTrustStore.password", idpTrustStore.getPassword()),
            config("serviceEntityIds", "[\"http://some-service-entity-id\"]"),
            config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            config("europeanIdentity.enabled", "false"),
            config("europeanIdentity.hubConnectorEntityId", "dummyEntity"),
            config("europeanIdentity.trustAnchorUri", "http://dummy.com"),
            config("europeanIdentity.metadataSourceUri", "http://dummy.com"),
            config("europeanIdentity.trustStore.path", metadataTrustStore.getAbsolutePath()),
            config("europeanIdentity.trustStore.password", metadataTrustStore.getPassword())
        );
    }

    private String getHubMetadataUrl() {
        return format("http://localhost:%s/SAML2/metadata", wireMockServer.port());
    }

    @After
    public void tearDown() {
        applicationTestSupport.after();
        wireMockServer.stop();
    }

    @Test
    public void shouldFailHealthcheckWhenHubMetadataUnavailable() {
        wireMockServer.stubFor(
            get(urlEqualTo("/SAML2/metadata"))
                .willReturn(aResponse()
                    .withStatus(500)
                )
        );

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = format("\"%s\":{\"healthy\":false", getHubMetadataUrl());

        wireMockServer.verify(getRequestedFor(urlEqualTo("/SAML2/metadata")));

        assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }

    @Test
    public void shouldFailHealthcheckWhenHubMetadataIsSignedWithMD5() {
        String id = UUID.randomUUID().toString();
        Signature signature = SignatureBuilder.aSignature()
            .withDigestAlgorithm(id, new DigestMD5())
            .withX509Data(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT)
            .withSigningCredential(new TestCredentialFactory(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT,
                    TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential()).build();
        String metadata = new MetadataFactory().metadata(new EntitiesDescriptorFactory().signedEntitiesDescriptor(id, signature));

        wireMockServer.stubFor(
                get(urlEqualTo("/SAML2/metadata"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody(metadata)
                        )
        );

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = format("\"%s\":{\"healthy\":false", getHubMetadataUrl());

        wireMockServer.verify(getRequestedFor(urlEqualTo("/SAML2/metadata")));

        assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }

    @Test
    public void shouldPassHealthcheckWhenHubMetadataAvailable() {
        wireMockServer.stubFor(
            get(urlEqualTo("/SAML2/metadata"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(new MetadataFactory().defaultMetadata())
                )
        );

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = format("\"%s\":{\"healthy\":true", getHubMetadataUrl());

        wireMockServer.verify(getRequestedFor(urlEqualTo("/SAML2/metadata")));

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }
}
