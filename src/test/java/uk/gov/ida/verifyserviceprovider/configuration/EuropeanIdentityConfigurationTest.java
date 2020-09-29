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

    public static final String IDATESTMETADATACA = "test-metadata-ca-g3";
    public static final String IDATESTROOTCA = "test-root-ca-g3";
    public static final String IDAPRODMETADATACAG3 = "prod-metadata-ca-g3";
    public static final String IDAPRODROOTCAG3 = "prod-root-ca-g3";
    public static final String OVERRIDDENMETADATACA = "overriddenmetadataca";
    public static final String OVERRIDDENROOTCA = "overriddenrootca";
    private final String overriddenTrustAnchorUri = "http://overridden.trustanchoruri.example.com";
    private final String overriddenMetadataSourceUri ="http://overridden.metadatsourceuri.example.com";
    private final String overriddenHubConnectorEntityId = "http://overridden.hubconnectorentityid.example.com";
    private String configEnabledOnly;
    private String configWithHubConnectorEntityIdOnly;
    private String configWithTrustAnchorUriOnly;
    private String configWithTrustStoreOnlyDefined;
    private String configWithMetadataSourceUri;

    private static KeyStoreResource overriddenKeyStoreResource;

    @Before
    public void setUp() {
        overriddenKeyStoreResource = KeyStoreResourceBuilder.aKeyStoreResource()
                .withCertificate(OVERRIDDENMETADATACA, CACertificates.TEST_VERIFY_METADATA_CA)
                .withCertificate(OVERRIDDENROOTCA, CACertificates.TEST_VERIFY_ROOT_CA).build();

        overriddenKeyStoreResource.create();

        configEnabledOnly = new JSONObject().put("enabled", true).toString();

        configWithHubConnectorEntityIdOnly = new JSONObject()
                .put("enabled", true)
                .put("hubConnectorEntityId",overriddenHubConnectorEntityId)
                .toString();

        configWithTrustAnchorUriOnly = new JSONObject()
                .put("enabled", true)
                .put("trustAnchorUri", overriddenTrustAnchorUri)
                .toString();

        configWithTrustStoreOnlyDefined = new JSONObject()
                .put("enabled", true)
                .put("trustStore", new JSONObject()
                        .put("path", overriddenKeyStoreResource.getAbsolutePath())
                        .put("password", overriddenKeyStoreResource.getPassword())
                )
                .toString();

        configWithMetadataSourceUri = new JSONObject()
                .put("enabled", true)
                .put("metadataSourceUri", overriddenMetadataSourceUri)
                .toString();

    }
    @Test
    public void shouldUseTestTrustStoreWithIntegrationTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToIntegration() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert = integrationKeyStore.getCertificate(IDATESTMETADATACA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configEnabledOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert = europeanIdentityConfiguration.getTrustStore().getCertificate(IDATESTMETADATACA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTROOTCA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTMETADATACA)).isTrue();
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);
    }

    @Test
    public void shouldUseIntegrationEnvironmentConfigExceptOverriddenHubConnectorEntityId() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert = integrationKeyStore.getCertificate(IDATESTMETADATACA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithHubConnectorEntityIdOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert = europeanIdentityConfiguration.getTrustStore().getCertificate(IDATESTMETADATACA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTROOTCA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTMETADATACA)).isTrue();
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);

        assertThat(europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds().toString()).contains(overriddenHubConnectorEntityId);
        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataSourceUri());
    }

    @Test
    public void shouldUseIntegrationEnvironmentConfigExceptOverriddenTrustAnchorUri() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert = integrationKeyStore.getCertificate(IDATESTMETADATACA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithTrustAnchorUriOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert = europeanIdentityConfiguration.getTrustStore().getCertificate(IDATESTMETADATACA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTROOTCA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTMETADATACA)).isTrue();
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);

        assertThat(europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds()).containsAll(HubEnvironment.INTEGRATION.getEidasDefaultAcceptableHubConnectorEntityIds());
        assertThat(europeanIdentityConfiguration.getTrustAnchorUri().toString()).isEqualTo(overriddenTrustAnchorUri);
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataSourceUri());
    }

    @Test
    public void shouldUseIntegrationEnvironmentConfigExceptOverriddenWithTrustStoreOnlyDefined() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  integrationKeyStore.getCertificate(IDATESTMETADATACA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithTrustStoreOnlyDefined, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(OVERRIDDENMETADATACA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(OVERRIDDENROOTCA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(OVERRIDDENMETADATACA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isNotEqualTo(integrationEntryCert);

        assertThat(europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds()).containsAll(HubEnvironment.INTEGRATION.getEidasDefaultAcceptableHubConnectorEntityIds());
        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataSourceUri());
    }

    @Test
    public void shouldUseIntegrationEnvironmentConfigExceptOverriddenWithMetadataSourceUriOnly() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  integrationKeyStore.getCertificate(IDATESTMETADATACA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithMetadataSourceUri, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDATESTMETADATACA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTROOTCA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTMETADATACA)).isTrue();
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);

        assertThat(europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds()).containsAll(HubEnvironment.INTEGRATION.getEidasDefaultAcceptableHubConnectorEntityIds());
        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.INTEGRATION.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri().toString()).isEqualTo(overriddenMetadataSourceUri);

    }

    @Test
    public void shouldUseTrustStoreWithProductionTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToProduction() throws Exception {

        KeyStore productionKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(PRODUCTION_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate prodMetadataCert = productionKeyStore.getCertificate(IDAPRODMETADATACAG3);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configEnabledOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.PRODUCTION);
        Certificate europeanConfigCert = europeanIdentityConfiguration.getTrustStore().getCertificate(IDAPRODMETADATACAG3);
        assertThat(productionKeyStore.containsAlias(IDAPRODMETADATACAG3)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAPRODROOTCAG3)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAPRODMETADATACAG3)).isTrue();
        assertThat(europeanConfigCert).isEqualTo(prodMetadataCert);
    }

    @Test
    public void shouldUseTestTrustStoreWithComplianceTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToCompliance() throws Exception {
        KeyStore integrationKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate integrationEntryCert =  integrationKeyStore.getCertificate(IDATESTMETADATACA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configEnabledOnly, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.COMPLIANCE_TOOL);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDATESTMETADATACA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTROOTCA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTMETADATACA)).isTrue();
        assertThat(europeanConfigCert).isEqualTo(integrationEntryCert);
    }

    @Test
    public void shouldUseProductionEnvironmentConfigExceptOverriddenWithMetadataSourceUriOnly() throws Exception {
        KeyStore productionKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(PRODUCTION_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate productionEntryCert =  productionKeyStore.getCertificate(IDAPRODMETADATACAG3);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithMetadataSourceUri, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.PRODUCTION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDAPRODMETADATACAG3);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAPRODROOTCAG3)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDAPRODMETADATACAG3)).isTrue();
        assertThat(europeanConfigCert).isEqualTo(productionEntryCert);

        assertThat(europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds()).containsAll(HubEnvironment.PRODUCTION.getEidasDefaultAcceptableHubConnectorEntityIds());
        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.PRODUCTION.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri().toString()).isEqualTo(overriddenMetadataSourceUri);
    }

    @Test
    public void shouldUseComplianceEnvironmentConfigExceptOverriddenWithMetadataSourceUriOnly() throws Exception {
        KeyStore complianceKeyStore = new KeyStoreLoader().load(ResourceHelpers.resourceFilePath(TEST_METADATA_TRUSTSTORE),DEFAULT_TRUST_STORE_PASSWORD);
        Certificate complianceEntryCert =  complianceKeyStore.getCertificate(IDATESTMETADATACA);

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(configWithMetadataSourceUri, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.COMPLIANCE_TOOL);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate(IDATESTMETADATACA);

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTROOTCA)).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias(IDATESTMETADATACA)).isTrue();
        assertThat(europeanConfigCert).isEqualTo(complianceEntryCert);

        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getEidasMetadataTrustAnchorUri());
        assertThat(europeanIdentityConfiguration.getMetadataSourceUri().toString()).isEqualTo(overriddenMetadataSourceUri);

    }
}
