package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.json.JSONObject;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.verifyserviceprovider.builders.ComplianceToolInitialisationRequestBuilder.aComplianceToolInitialisationRequest;

public class AuthnRequestAcceptanceTest {

    private static String COMPLIANCE_TOOL_HOST = "http://localhost:50270";
    private static String SINGLE_ENTITY_ID = "http://default-entity-id";
    private static String MULTI_ENTITY_ID_1 = "http://service-entity-id-one";
    private static String MULTI_ENTITY_ID_2 = "http://service-entity-id-two";

    public static final DropwizardTestSupport<VerifyServiceProviderConfiguration> singleTenantApplication = new DropwizardTestSupport<>(
        VerifyServiceProviderApplication.class,
        "verify-service-provider.yml",
        ConfigOverride.config("server.connector.port", String.valueOf(0)),
        ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
        ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
        ConfigOverride.config("serviceEntityIds", SINGLE_ENTITY_ID),
        ConfigOverride.config("msaMetadata.expectedEntityId", "some-msa-expected-entity-id"),
        ConfigOverride.config("msaMetadata.uri", "http://some-msa-uri"),
        ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY)
    );

    public static final DropwizardTestSupport<VerifyServiceProviderConfiguration> multiTenantApplication = new DropwizardTestSupport<>(
        VerifyServiceProviderApplication.class,
        "verify-service-provider.yml",
        ConfigOverride.config("server.connector.port", String.valueOf(0)),
        ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
        ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
        ConfigOverride.config("serviceEntityIds", String.format("%s,%s", MULTI_ENTITY_ID_1, MULTI_ENTITY_ID_2)),
        ConfigOverride.config("msaMetadata.expectedEntityId", "some-msa-expected-entity-id"),
        ConfigOverride.config("msaMetadata.uri", "http://some-msa-uri"),
        ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY)
    );


    @Test
    public void shouldGenerateValidAuthnRequestUsingDefaultEntityId() throws Exception {
        singleTenantApplication.before();
        Client client = new JerseyClientBuilder(singleTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithDefaultEntityId(client);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", singleTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null)))
            .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);

        Response complianceToolResponse = client
            .target(authnSaml.getSsoLocation())
            .request()
            .buildPost(Entity.form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", authnSaml.getSamlRequest()))))
            .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolResponse.readEntity(String.class));
        assertThat(complianceToolResponseBody.getJSONObject("status").get("message")).isEqualTo(null);
        assertThat(complianceToolResponseBody.getJSONObject("status").getString("status")).isEqualTo("PASSED");

        singleTenantApplication.after();
    }

    @Test
    public void shouldGenerateValidAuthnRequestWhenPassedAnEntityId() throws Exception {
        multiTenantApplication.before();
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(LevelOfAssurance.LEVEL_2, MULTI_ENTITY_ID_1)))
            .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);

        Response complianceToolResponse = client
            .target(authnSaml.getSsoLocation())
            .request()
            .buildPost(Entity.form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", authnSaml.getSamlRequest()))))
            .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolResponse.readEntity(String.class));
        assertThat(complianceToolResponseBody.getJSONObject("status").get("message")).isEqualTo(null);
        assertThat(complianceToolResponseBody.getJSONObject("status").getString("status")).isEqualTo("PASSED");

        multiTenantApplication.after();
    }

    @Test
    public void shouldReturn400WhenPassedNoEntityIdForMultiTenantApplication() throws Exception {
        multiTenantApplication.before();
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null)))
            .invoke();

        assertThat(authnResponse.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        multiTenantApplication.after();
    }

    @Test
    public void shouldReturn400WhenPassedInvalidEntityIdForMultiTenantApplication() throws Exception {
        multiTenantApplication.before();
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(LevelOfAssurance.LEVEL_2, "not a valid entityID")))
            .invoke();

        assertThat(authnResponse.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        multiTenantApplication.after();
    }

    private void setupComplianceToolWithDefaultEntityId(Client client) throws Exception {
        setupComplianceToolWithEntityId(client, SINGLE_ENTITY_ID);
    }

    private void setupComplianceToolWithEntityId(Client client, String entityId) throws Exception {
        Entity initializationRequest = aComplianceToolInitialisationRequest().withEntityId(entityId).build();

        Response complianceToolResponse = client
            .target(URI.create(String.format("%s/%s", COMPLIANCE_TOOL_HOST, "service-test-data")))
            .request()
            .buildPost(initializationRequest)
            .invoke();

        assertThat(complianceToolResponse.getStatus()).isEqualTo(OK.getStatusCode());
    }
}
