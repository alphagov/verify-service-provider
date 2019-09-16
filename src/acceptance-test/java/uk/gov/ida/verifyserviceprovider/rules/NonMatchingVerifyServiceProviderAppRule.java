package uk.gov.ida.verifyserviceprovider.rules;

import certificates.values.CACertificates;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMetadataAggregatorServer;
import common.uk.gov.ida.verifyserviceprovider.servers.MockTrustAnchorServer;
import common.uk.gov.ida.verifyserviceprovider.servers.MockVerifyHubServer;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.junit.rules.RuleChain;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP;

public class NonMatchingVerifyServiceProviderAppRule extends DropwizardAppRule<VerifyServiceProviderConfiguration> {

    public static final String COUNTRY_ENTITY_ID = "https://localhost:12345/metadata-aggregator/test-country";

    private static final MockMetadataAggregatorServer metadataAggregatorServer = new MockMetadataAggregatorServer();
    private static final MockTrustAnchorServer trustAnchorServer = new MockTrustAnchorServer();
    private static final MockVerifyHubServer verifyMetadataServer = new MockVerifyHubServer();

    private static final KeyStoreResource countryMetadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource hubTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("hubCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource idpTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    public NonMatchingVerifyServiceProviderAppRule() {
        super(
                VerifyServiceProviderApplication.class,
                "configuration/vsp-no-eidas.yml",
                ConfigOverride.config("serviceEntityIds", TEST_RP),
                ConfigOverride.config("hashingEntityId", "some-hashing-entity-id"),
                ConfigOverride.config("server.connector.port", String.valueOf(0)),
                ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
                ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
                ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
                ConfigOverride.config("verifyHubConfiguration.metadata.uri", verifyMetadataServer::getUri),
                ConfigOverride.config("verifyHubConfiguration.metadata.trustStore.path", metadataTrustStore.getAbsolutePath()),
                ConfigOverride.config("verifyHubConfiguration.metadata.trustStore.password", metadataTrustStore.getPassword()),
                ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY)
        );
    }

    public NonMatchingVerifyServiceProviderAppRule(boolean isEidasEnabled) {
        super(
                VerifyServiceProviderApplication.class,
                "verify-service-provider.yml",
                ConfigOverride.config("serviceEntityIds", TEST_RP),
                ConfigOverride.config("hashingEntityId", "some-hashing-entity-id"),
                ConfigOverride.config("server.connector.port", String.valueOf(0)),
                ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
                ConfigOverride.config("logging.level", "WARN"),
                ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
                ConfigOverride.config("verifyHubConfiguration.environment", "COMPLIANCE_TOOL"),
                ConfigOverride.config("verifyHubConfiguration.metadata.uri", verifyMetadataServer::getUri),
                ConfigOverride.config("verifyHubConfiguration.metadata.trustStore.path", metadataTrustStore.getAbsolutePath()),
                ConfigOverride.config("verifyHubConfiguration.metadata.trustStore.password", metadataTrustStore.getPassword()),
                ConfigOverride.config("verifyHubConfiguration.metadata.hubTrustStore.path", hubTrustStore.getAbsolutePath()),
                ConfigOverride.config("verifyHubConfiguration.metadata.hubTrustStore.password", hubTrustStore.getPassword()),
                ConfigOverride.config("verifyHubConfiguration.metadata.idpTrustStore.path", idpTrustStore.getAbsolutePath()),
                ConfigOverride.config("verifyHubConfiguration.metadata.idpTrustStore.password", idpTrustStore.getPassword()),
                ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY),
                ConfigOverride.config("europeanIdentity.hubConnectorEntityId", HUB_CONNECTOR_ENTITY_ID),
                ConfigOverride.config("europeanIdentity.enabled", isEidasEnabled ? "true" : "false"),
                ConfigOverride.config("europeanIdentity.trustAnchorUri", trustAnchorServer::getUri),
                ConfigOverride.config("europeanIdentity.metadataSourceUri", metadataAggregatorServer::getUri),
                ConfigOverride.config("europeanIdentity.trustStore.path", countryMetadataTrustStore.getAbsolutePath()),
                ConfigOverride.config("europeanIdentity.trustStore.password", countryMetadataTrustStore.getPassword())
        );
    }

    @Override
    protected void before() {
        countryMetadataTrustStore.create();
        metadataTrustStore.create();
        hubTrustStore.create();
        idpTrustStore.create();

        try {
            InitializationService.initialize();

            verifyMetadataServer.serveDefaultMetadata();

            trustAnchorServer.serveTrustAnchor(COUNTRY_ENTITY_ID);

            metadataAggregatorServer.serveAggregatedMetadata(COUNTRY_ENTITY_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.before();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return RuleChain.outerRule(verifyMetadataServer)
            .around(trustAnchorServer)
            .around(metadataAggregatorServer)
            .around(super::apply).apply(base, description);
    }

    public String getCountryEntityId() {
        return COUNTRY_ENTITY_ID;
    }
}