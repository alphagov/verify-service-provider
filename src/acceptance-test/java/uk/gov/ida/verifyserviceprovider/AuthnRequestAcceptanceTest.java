package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;
import static uk.gov.ida.verifyserviceprovider.builders.ComplianceToolV1InitialisationRequestBuilder.aComplianceToolV1InitialisationRequest;

public class AuthnRequestAcceptanceTest {

    private static String COMPLIANCE_TOOL_HOST = "https://compliance-tool-integration.cloudapps.digital";
    private static String SINGLE_ENTITY_ID = "http://default-entity-id";
    private static String HASHING_ENTITY_ID = "http://default-entity-id";
    private static String MULTI_ENTITY_ID_1 = "http://service-entity-id-one";
    private static String MULTI_ENTITY_ID_2 = "http://service-entity-id-two";

    private static final KeyStoreResource KEY_STORE_RESOURCE = aKeyStoreResource()
        .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
        .build();
    static {
        KEY_STORE_RESOURCE.create();
    }

    @Rule
    public final DropwizardAppRule<VerifyServiceProviderConfiguration> singleTenantApplication = new DropwizardAppRule<>(
        VerifyServiceProviderApplication.class,
        "verify-service-provider.yml",
        ConfigOverride.config("server.connector.port", String.valueOf(0)),
        ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
        ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
        ConfigOverride.config("serviceEntityIds", SINGLE_ENTITY_ID),
        ConfigOverride.config("hashingEntityId", HASHING_ENTITY_ID),
        ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
        ConfigOverride.config("europeanIdentity.enabled", "false"),
        ConfigOverride.config("europeanIdentity.hubConnectorEntityId", "dummyEntity"),
        ConfigOverride.config("europeanIdentity.trustAnchorUri", "http://dummy.com"),
        ConfigOverride.config("europeanIdentity.metadataSourceUri", "http://dummy.com"),
        ConfigOverride.config("europeanIdentity.trustStore.path", KEY_STORE_RESOURCE.getAbsolutePath()),
        ConfigOverride.config("europeanIdentity.trustStore.password", KEY_STORE_RESOURCE.getPassword())
    );

    @Rule
    public final DropwizardAppRule<VerifyServiceProviderConfiguration> multiTenantApplication = new DropwizardAppRule<>(
        VerifyServiceProviderApplication.class,
        "verify-service-provider.yml",
        ConfigOverride.config("server.connector.port", String.valueOf(0)),
        ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
        ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
        ConfigOverride.config("serviceEntityIds", String.format("%s,%s", MULTI_ENTITY_ID_1, MULTI_ENTITY_ID_2)),
        ConfigOverride.config("hashingEntityId", HASHING_ENTITY_ID),
        ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
        ConfigOverride.config("europeanIdentity.enabled", "false"),
        ConfigOverride.config("europeanIdentity.hubConnectorEntityId", "dummyEntity"),
        ConfigOverride.config("europeanIdentity.trustAnchorUri", "http://dummy.com"),
        ConfigOverride.config("europeanIdentity.metadataSourceUri", "http://dummy.com"),
        ConfigOverride.config("europeanIdentity.trustStore.path", KEY_STORE_RESOURCE.getAbsolutePath()),
        ConfigOverride.config("europeanIdentity.trustStore.password", KEY_STORE_RESOURCE.getPassword())
    );

    @Test
    public void shouldGenerateValidAuthnRequestWhenNoParams() throws Exception {
        Client client = new JerseyClientBuilder(singleTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithDefaultEntityId(client);

        Response generateRequestResponse = client
                .target(URI.create(String.format("http://localhost:%d/generate-request", singleTenantApplication.getLocalPort())))
                .request()
                .buildPost(Entity.json(null))
                .invoke();

        RequestResponseBody authnSaml = generateRequestResponse.readEntity(RequestResponseBody.class);
        assertThat(StringUtils.isNotBlank(authnSaml.getRequestId()));

        Response complianceToolResponse = client
                .target(authnSaml.getSsoLocation())
                .request()
                .buildPost(Entity.form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", authnSaml.getSamlRequest()))))
                .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolResponse.readEntity(String.class));
        assertThat(complianceToolResponseBody.getJSONObject("status").get("message")).isEqualTo(null);
        assertThat(complianceToolResponseBody.getJSONObject("status").getString("status")).isEqualTo("PASSED");
    }


    @Test
    public void shouldGenerateValidAuthnRequestUsingDefaultEntityId() throws Exception {
        Client client = new JerseyClientBuilder(singleTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithDefaultEntityId(client);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", singleTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(null)))
            .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);
        assertThat(StringUtils.isNotBlank(authnSaml.getRequestId()));

        Response complianceToolResponse = client
            .target(authnSaml.getSsoLocation())
            .request()
            .buildPost(Entity.form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", authnSaml.getSamlRequest()))))
            .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolResponse.readEntity(String.class));
        assertThat(complianceToolResponseBody.getJSONObject("status").get("message")).isEqualTo(null);
        assertThat(complianceToolResponseBody.getJSONObject("status").getString("status")).isEqualTo("PASSED");
    }

    @Test
    public void shouldGenerateValidAuthnRequestWhenPassedAnEntityId() throws Exception {
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(MULTI_ENTITY_ID_1)))
            .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);
        assertThat(StringUtils.isNotBlank(authnSaml.getRequestId()));

        Response complianceToolResponse = client
            .target(authnSaml.getSsoLocation())
            .request()
            .buildPost(Entity.form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", authnSaml.getSamlRequest()))))
            .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolResponse.readEntity(String.class));
        assertThat(complianceToolResponseBody.getJSONObject("status").get("message")).isEqualTo(null);
        assertThat(complianceToolResponseBody.getJSONObject("status").getString("status")).isEqualTo("PASSED");
    }

    @Test
    public void shouldReturn400WhenPassedNoEntityIdForMultiTenantApplication() throws Exception {
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(null)))
            .invoke();

        assertThat(authnResponse.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldReturn400WhenPassedInvalidEntityIdForMultiTenantApplication() throws Exception {
        Client client = new JerseyClientBuilder(multiTenantApplication.getEnvironment()).build("Test Client");

        setupComplianceToolWithEntityId(client, MULTI_ENTITY_ID_1);

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", multiTenantApplication.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody("not a valid entityID")))
            .invoke();

        assertThat(authnResponse.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    private void setupComplianceToolWithDefaultEntityId(Client client) throws Exception {
        setupComplianceToolWithEntityId(client, SINGLE_ENTITY_ID);
    }

    private void setupComplianceToolWithEntityId(Client client, String entityId) throws Exception {
        Entity initializationRequest = aComplianceToolV1InitialisationRequest().withEntityId(entityId).build();

        Response complianceToolResponse = client
            .target(URI.create(String.format("%s/%s", COMPLIANCE_TOOL_HOST, "service-test-data")))
            .request()
            .buildPost(initializationRequest)
            .invoke();

        assertThat(complianceToolResponse.getStatus()).isEqualTo(OK.getStatusCode());
    }
}
