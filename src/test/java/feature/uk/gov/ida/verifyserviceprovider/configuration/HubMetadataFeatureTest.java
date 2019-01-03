package feature.uk.gov.ida.verifyserviceprovider.configuration;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableList;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import feature.uk.gov.ida.verifyserviceprovider.configuration.support.MSAStubRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import keystore.CertificateEntry;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.xmlsec.algorithm.descriptors.DigestMD5;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class HubMetadataFeatureTest {

    private final String HEALTHCHECK_URL = "http://localhost:%d/admin/healthcheck";

    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    private MSAStubRule msaStubRule;
    private DropwizardTestSupport<VerifyServiceProviderConfiguration> applicationTestSupport;

    @Before
    public void setUp() throws MarshallingException, SignatureException, JsonProcessingException {
        msaStubRule = new MSAStubRule();
        KeyStoreResource verifyHubKeystoreResource = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();
        verifyHubKeystoreResource.create();
        KeyStoreResource msTrustStore = KeyStoreResourceBuilder.aKeyStoreResource()
                .withCertificates(ImmutableList.of(new CertificateEntry("test_root_ca", CACertificates.TEST_ROOT_CA),
                        new CertificateEntry("test_rp_ca", CACertificates.TEST_RP_CA)))
                .build();
        msTrustStore.create();
        applicationTestSupport = new DropwizardTestSupport<>(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            config("server.connector.port", "0"),
            config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            config("verifyHubConfiguration.metadata.uri", () -> String.format("http://localhost:%s/SAML2/metadata", wireMockServer.port())),
            config("msaMetadata.uri", () -> msaStubRule.METADATA_ENTITY_ID),
            config("msaMetadata.expectedEntityId", msaStubRule.METADATA_ENTITY_ID),
            config("verifyHubConfiguration.metadata.expectedEntityId", HUB_ENTITY_ID),
            config("verifyHubConfiguration.metadata.trustStore.path", verifyHubKeystoreResource.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.trustStore.password", verifyHubKeystoreResource.getPassword()),
            config("serviceEntityIds", "[\"http://some-service-entity-id\"]"),
            config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            config("samlPrimarySigningCert.cert", TEST_RP_PUBLIC_SIGNING_CERT.replaceAll("\n", "")),
            config("samlPrimaryEncryptionCert.cert", TEST_RP_PUBLIC_ENCRYPTION_CERT.replaceAll("\n", "")),
            config("msaMetadata.trustStore.type", "file"),
            config("msaMetadata.trustStore.store", msTrustStore.getAbsolutePath()),
            config("msaMetadata.trustStore.password", msTrustStore.getPassword()),
            config("samlPrimarySigningCert.type", "x509"),
            config("samlPrimaryEncryptionCert.type", "x509")
        );

        IdaSamlBootstrap.bootstrap();
        msaStubRule.setUpRegularMetadata();
        wireMockServer.start();
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
            .target(URI.create(String.format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = "\"hubMetadata\":{\"healthy\":false";

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
            .target(URI.create(String.format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = "\"hubMetadata\":{\"healthy\":false";

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
            .target(URI.create(String.format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = "\"hubMetadata\":{\"healthy\":true";

        wireMockServer.verify(getRequestedFor(urlEqualTo("/SAML2/metadata")));

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }
}
