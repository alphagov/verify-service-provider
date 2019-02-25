package uk.gov.ida.verifyserviceprovider.configuration;

import certificates.values.CACertificates;
import helpers.ResourceHelpers;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.truststore.KeyStoreLoader;

import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.DEFAULT_TRUST_STORE_PASSWORD;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_METADATA_TRUSTSTORE;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_METADATA_TRUSTSTORE;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class EuropeanIdentityConfigurationTest {

    public static final String IDAMETADATA = "Idametadata";
    public static final String IDACA = "Idaca";
    public static final String OVERRIDENMETADATACA = "overridenmetadataca";
    public static final String OVERRIDENROOTCA = "overridenrootca";
    private final String overridenTrustAnchorUri = "http://overriden.trustanchoruri.example.com";
    private final String overridenMetadataSourceUri ="http://overriden.metadatsourceuri.example.com";
    private String configEnabledOnly, configWithTrustAnchorUriOnly,configWithTrustStoreOnlyDefined, configWithWithMetadataSourceUri;

    private static KeyStoreResource overridenKeyStoreResource;

    private String configWithEmptyAggregataMetadata;
    public static final String IDAMETADATAG2 = "idametadatag2";

    @Before
    public void setUp() {
        overridenKeyStoreResource = KeyStoreResourceBuilder.aKeyStoreResource()
                .withCertificate(OVERRIDENMETADATACA, CACertificates.TEST_METADATA_CA)
                .withCertificate(OVERRIDENROOTCA, CACertificates.TEST_ROOT_CA).build();

        overridenKeyStoreResource.create();

        configEnabledOnly = new JSONObject().put("enabled", true).toString();

        configWithTrustAnchorUriOnly = new JSONObject()
                .put("enabled", true)
                .put("hubConnectorEntityId","some-entity-id")
                .put("aggregatedMetadata", new JSONObject()
                    .put("trustAnchorUri", overridenTrustAnchorUri)
                ).toString();

        configWithTrustStoreOnlyDefined = new JSONObject()
                .put("enabled", true)
                .put("hubConnectorEntityId","some-entity-id")
                .put("aggregatedMetadata", new JSONObject()
                        .put("trustStore", new JSONObject()
                                .put("path", overridenKeyStoreResource.getAbsolutePath())
                                .put("password", overridenKeyStoreResource.getPassword())
                        )
                ).toString();

        configWithWithMetadataSourceUri = new JSONObject()
                .put("enabled", true)
                .put("hubConnectorEntityId","some-entity-id")
                .put("aggregatedMetadata", new JSONObject()
                        .put("metadataSourceUri",overridenMetadataSourceUri)
                ).toString();

        configWithEmptyAggregataMetadata = new JSONObject()
                .put("enabled", true)
                .put("hubConnectorEntityId","some-entity-id")
                .put("aggregatedMetadata", new JSONObject()
                ).toString();

    }
    @Test
    public void shouldUseTestTrustStoreWithIntegrationTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToIntegration() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  integrationKeyStore.getCertificate(IDAMETADATA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configEnabledOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDAMETADATA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAMETADATA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);
    }

    @Test
    public void shouldUseIntegrationEnvironmentConfigExceptOverriddenTrustAnchorUri() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  integrationKeyStore.getCertificate(IDAMETADATA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithTrustAnchorUriOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDAMETADATA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAMETADATA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);

        assertThat(europeanIdentityConfiguration.getTrustAnchorUri().toString()).isEqualTo(overridenTrustAnchorUri);
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataSourceUri());
    }

    @Test
    public void shouldUseIntegrationEnvironmentConfigExceptOverriddenWithTrustStoreOnlyDefined() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  integrationKeyStore.getCertificate(IDAMETADATA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithTrustStoreOnlyDefined, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(OVERRIDENMETADATACA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(OVERRIDENROOTCA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(OVERRIDENMETADATACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isNotEqualTo(integrationEntryCert);

        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataSourceUri());
    }

    @Test
    public void shouldUseIntegrationEnvironmentConfigExceptOverriddenWithMetadataSourceUriOnly() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  integrationKeyStore.getCertificate(IDAMETADATA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithWithMetadataSourceUri, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDAMETADATA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAMETADATA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);

        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri().toString()).isEqualTo(overridenMetadataSourceUri);

    }

    @Test
    public void shouldUseTrustStoreWithProductionTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToProduction() throws Exception {

        KeyStore productionKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(PRODUCTION_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  productionKeyStore.getCertificate(IDAMETADATAG2);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configEnabledOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.PRODUCTION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDAMETADATAG2);
        assertThat(productionKeyStore.containsAlias(IDAMETADATAG2)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAMETADATAG2)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);
    }

    @Test
    public void shouldUseTestTrustStoreWithComplianceTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToCompliance() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  integrationKeyStore.getCertificate(IDAMETADATA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configEnabledOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.COMPLIANCE_TOOL);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDAMETADATA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAMETADATA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);
    }

    @Test
    public void shouldUseProductionEnvironmentConfigExceptOverriddenWithMetadataSourceUriOnly() throws Exception {
        KeyStore productionKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(PRODUCTION_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate productionEntryCert =  productionKeyStore.getCertificate(IDAMETADATAG2);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithWithMetadataSourceUri, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.PRODUCTION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDAMETADATAG2);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAMETADATAG2)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(productionEntryCert);

        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.PRODUCTION.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri().toString()).isEqualTo(overridenMetadataSourceUri);
    }

    @Test
    public void shouldUseComplianceEnvironmentConfigExceptOverriddenWithMetadataSourceUriOnly() throws Exception {
        KeyStore complianceKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate complianceEntryCert =  complianceKeyStore.getCertificate(IDAMETADATA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithWithMetadataSourceUri, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.COMPLIANCE_TOOL);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDAMETADATA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAMETADATA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(complianceEntryCert);

        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri().toString()).isEqualTo(overridenMetadataSourceUri);

    }
}
