package uk.gov.ida.verifyserviceprovider.rules;

import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;

public class VerifyServiceProviderAppRule extends DropwizardAppRule<VerifyServiceProviderConfiguration> {


    public VerifyServiceProviderAppRule(String secondaryEncryptionKey, String serviceEntityIdOverride) {
        super(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            ConfigOverride.config("serviceEntityIds", serviceEntityIdOverride),
            ConfigOverride.config("hashingEntityId", "some-hashing-entity-id"),
            ConfigOverride.config("server.connector.port", String.valueOf(0)),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("samlSecondaryEncryptionKey", secondaryEncryptionKey),
            ConfigOverride.config("msaMetadata.uri", "http://some-msa-uri")
        );
    }
    public VerifyServiceProviderAppRule(MockMsaServer msaServer, String secondaryEncryptionKey, String serviceEntityIdOverride) {
        super(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml",
            ConfigOverride.config("serviceEntityIds", serviceEntityIdOverride),
            ConfigOverride.config("hashingEntityId", "some-hashing-entity-id"),
            ConfigOverride.config("server.connector.port", String.valueOf(0)),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
            ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
            ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
            ConfigOverride.config("samlSecondaryEncryptionKey", secondaryEncryptionKey),
            ConfigOverride.config("msaMetadata.uri", () -> {
                IdaSamlBootstrap.bootstrap();
                msaServer.serveDefaultMetadata();
                return msaServer.getUri();
            }),
            ConfigOverride.config("msaMetadata.expectedEntityId", MockMsaServer.MSA_ENTITY_ID)
        );
    }

    public VerifyServiceProviderAppRule() {
        this(TEST_RP_PRIVATE_ENCRYPTION_KEY, "http://verify-service-provider");
    }

    public VerifyServiceProviderAppRule(MockMsaServer msaServer) {
        this(msaServer,TEST_RP_PRIVATE_ENCRYPTION_KEY, "http://verify-service-provider");
    }

    public VerifyServiceProviderAppRule(MockMsaServer msaServer, String serviceEntityIdOverride) {
        this(msaServer,TEST_RP_PRIVATE_ENCRYPTION_KEY, serviceEntityIdOverride);
    }
}
