package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;
import uk.gov.ida.verifyserviceprovider.services.GenerateRequestService;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Map;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.verifyserviceprovider.builders.ComplianceToolInitialisationRequestBuilder.aComplianceToolInitialisationRequest;
import static uk.gov.ida.verifyserviceprovider.configuration.MetadataUri.COMPLIANCE_TOOL;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class MatchResponseAcceptanceTest {

    @ClassRule
    public static DropwizardAppRule<VerifyServiceProviderConfiguration> application = new DropwizardAppRule<>(
        VerifyServiceProviderApplication.class,
        resourceFilePath("verify-service-provider-acceptance-test.yml"),
        ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("hubSsoLocation", ComplianceToolService.SSO_LOCATION),
        ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
        ConfigOverride.config("verifyHubMetadata.uri", COMPLIANCE_TOOL.getUri().toString())
    );

    private static Client client = application.client();
    private static ComplianceToolService complianceTool = new ComplianceToolService(client);
    private static GenerateRequestService generateRequestService = new GenerateRequestService(
        client,
        complianceTool
    );

    @Test
    public void shouldHandleASuccessMatchResponse() {
        complianceTool.initialiseWith(
            aComplianceToolInitialisationRequest()
                .withEncryptionCertificate(TEST_RP_PUBLIC_ENCRYPTION_CERT)
                .withExpectedPid("some-expected-pid")
                .build()
        );

        Map<String, String> translateResponseRequestData = ImmutableMap.of(
            "samlResponse", generateRequestService.generateSuccessMatchSamlResponseString(application.getLocalPort()),
            "requestId", "string-to-be-fixed"
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(TranslatedResponseBody.class)).isEqualTo(new TranslatedResponseBody(
            "MATCH",
            "some-expected-pid",
            LEVEL_2,
            null)
        );
    }
}
