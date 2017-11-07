package feature.uk.gov.ida.verifyserviceprovider.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import common.uk.gov.ida.verifyserviceprovider.servers.MockVerifyHubServer;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import keystore.KeyStoreResource;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class MsaMetadataFeatureTest {

    private final String HEALTHCHECK_URL = "http://localhost:%d/admin/healthcheck";

    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    @ClassRule
    public static MockVerifyHubServer hubServer = new MockVerifyHubServer();

    private DropwizardTestSupport<VerifyServiceProviderConfiguration> applicationTestSupport;

    @Before
    public void setUp() {
        KeyStoreResource verifyHubKeystoreResource = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();
        verifyHubKeystoreResource.create();
        this.applicationTestSupport = new DropwizardTestSupport<>(
            VerifyServiceProviderApplication.class,
            resourceFilePath("verify-service-provider.yml"),
            config("verifyHubConfiguration.metadata.uri", () -> String.format("http://localhost:%s/SAML2/metadata", hubServer.port())),
            config("msaMetadata.uri", () -> String.format("http://localhost:%s/matching-service/metadata", wireMockServer.port())),
            config("verifyHubConfiguration.metadata.expectedEntityId", HUB_ENTITY_ID),
            config("msaMetadata.expectedEntityId", MockMsaServer.MSA_ENTITY_ID),
            config("verifyHubConfiguration.metadata.trustStore.path", verifyHubKeystoreResource.getAbsolutePath()),
            config("verifyHubConfiguration.metadata.trustStore.password", verifyHubKeystoreResource.getPassword())
        );
        IdaSamlBootstrap.bootstrap();
        wireMockServer.start();
        hubServer.serveDefaultMetadata();
    }

    @After
    public void tearDown() {
        applicationTestSupport.after();
        wireMockServer.stop();
    }

    @Test
    public void shouldFailHealthcheckWhenMsaMetadataUnavailable() {
        wireMockServer.stubFor(
            get(urlEqualTo("/matching-service/metadata"))
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

        String expectedResult = "\"msaMetadata\":{\"healthy\":false";

        wireMockServer.verify(getRequestedFor(urlEqualTo("/matching-service/metadata")));

        assertThat(response.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }

    @Test
    public void shouldPassHealthcheckWhenMsaMetadataAvailable() {
        wireMockServer.stubFor(
            get(urlEqualTo("/matching-service/metadata"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(MockMsaServer.msaMetadata())
                )
        );

        applicationTestSupport.before();
        Client client = new JerseyClientBuilder(applicationTestSupport.getEnvironment()).build("test client");

        Response response = client
            .target(URI.create(String.format(HEALTHCHECK_URL, applicationTestSupport.getLocalPort())))
            .request()
            .buildGet()
            .invoke();

        String expectedResult = "\"msaMetadata\":{\"healthy\":true";

        wireMockServer.verify(getRequestedFor(urlEqualTo("/matching-service/metadata")));

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(expectedResult);
    }
}
