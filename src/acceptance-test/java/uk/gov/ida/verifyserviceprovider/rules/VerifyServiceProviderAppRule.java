package uk.gov.ida.verifyserviceprovider.rules;

import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.verifyserviceprovider.configuration.MetadataUri.COMPLIANCE_TOOL;

public class VerifyServiceProviderAppRule extends DropwizardAppRule<VerifyServiceProviderConfiguration> {

    public VerifyServiceProviderAppRule(MockMsaServer msaServer, String secondaryEncryptionKey) {
        super(
            VerifyServiceProviderApplication.class,
            resourceFilePath("verify-service-provider.yml"),
            ConfigOverride.config("server.connector.port", String.valueOf(0)),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("hubSsoLocation", ComplianceToolService.SSO_LOCATION),
            ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("samlSecondaryEncryptionKey", secondaryEncryptionKey),
            ConfigOverride.config("verifyHubMetadata.uri", COMPLIANCE_TOOL.getUri().toString()),
            ConfigOverride.config("msaMetadata.uri", () -> {
                IdaSamlBootstrap.bootstrap();
                msaServer.serveDefaultMetadata();
                return msaServer.getUri();
            })
        );
    }

    public VerifyServiceProviderAppRule(MockMsaServer msaServer) {
        this(msaServer, TEST_RP_PRIVATE_ENCRYPTION_KEY);
    }
}
