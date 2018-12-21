package feature.uk.gov.ida.verifyserviceprovider.configuration;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import common.uk.gov.ida.verifyserviceprovider.servers.MockVerifyHubServer;
import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import feature.uk.gov.ida.verifyserviceprovider.configuration.support.MSAStubRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import keystore.CertificateEntry;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;

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

public class MsaMetadataFeatureTest {

    private static KeyStoreResource msTrustStore;

    @BeforeClass
    public static void setupTrustStores() {
        msTrustStore = KeyStoreResourceBuilder.aKeyStoreResource()
                .withCertificates(ImmutableList.of(new CertificateEntry("test_root_ca", CACertificates.TEST_ROOT_CA),
                        new CertificateEntry("test_rp_ca", CACertificates.TEST_RP_CA)))
                .build();
        msTrustStore.create();
    }

    private final String HEALTHCHECK_URL = "http://localhost:%d/admin/healthcheck";

    private MSAStubRule msaStubRule;
    @ClassRule
    public static MockVerifyHubServer hubServer = new MockVerifyHubServer();

    private DropwizardTestSupport<VerifyServiceProviderConfiguration> applicationTestSupport;
    private EnvironmentHelper environmentHelper = new EnvironmentHelper();

    @Before
    public void setUp() {
        msaStubRule = new MSAStubRule();
        KeyStoreResource verifyHubKeystoreResource = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();
        verifyHubKeystoreResource.create();
        this.applicationTestSupport = new DropwizardTestSupport<>(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            config("server.connector.port", "0"),
            config("verifyHubConfiguration.metadata.uri", () -> String.format("http://localhost:%s/SAML2/metadata", hubServer.port())),
            config("msaMetadata.uri", () -> msaStubRule.METADATA_ENTITY_ID),
            config("verifyHubConfiguration.metadata.expectedEntityId", HUB_ENTITY_ID),
            config("msaMetadata.expectedEntityId", msaStubRule.METADATA_ENTITY_ID),
            config("verifyHubConfiguration.metadata.trustStore.path", verifyHubKeystoreResource.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.trustStore.password", verifyHubKeystoreResource.getPassword()),
            config("msaMetadata.trustStore.type", "file"),
            config("msaMetadata.trustStore.store", msTrustStore.getAbsolutePath()),
            config("msaMetadata.trustStore.password", msTrustStore.getPassword()),
            config("samlPrimarySigningCert.type", "x509"),
            config("samlPrimaryEncryptionCert.type", "x509")
        );

        environmentHelper.setEnv(new HashMap<String, String>() {{
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_SIGNING_CERT", TEST_RP_PUBLIC_SIGNING_CERT.replaceAll("\n", ""));
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("SAML_PRIMARY_ENCRYPTION_CERT", TEST_RP_PUBLIC_ENCRYPTION_CERT.replaceAll("\n", ""));
        }});

        IdaSamlBootstrap.bootstrap();
        hubServer.serveDefaultMetadata();
    }

    @After
    public void tearDown() {
        applicationTestSupport.after();
    }

    @Test
    public void shouldFailHealthcheckWhenMsaMetadataUnavailable() throws JsonProcessingException {
        msaStubRule.setUpMissingMetadata();

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(String.format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = "\"msaMetadata\":{\"healthy\":false";

        msaStubRule.getLastRequest().getUrl().equals(msaStubRule.METADATA_ENTITY_ID);

        assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }

    @Test
    public void shouldPassHealthcheckWhenMsaMetadataAvailable() throws MarshallingException, SignatureException, JsonProcessingException {
        msaStubRule.setUpRegularMetadata();

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(String.format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = "\"msaMetadata\":{\"healthy\":true";

        msaStubRule.getLastRequest().getUrl().equals(msaStubRule.METADATA_ENTITY_ID);

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }
}
