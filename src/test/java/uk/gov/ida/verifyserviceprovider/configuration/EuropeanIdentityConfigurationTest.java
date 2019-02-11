package uk.gov.ida.verifyserviceprovider.configuration;

import certificates.values.CACertificates;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.saml.metadata.EidasMetadataConfiguration;

import java.io.IOException;
import java.net.URI;
import java.security.cert.Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class EuropeanIdentityConfigurationTest {

    private String config;
    private static final KeyStoreResource keyStore = KeyStoreResourceBuilder.aKeyStoreResource()
            .withCertificate("metadataCA", CACertificates.TEST_METADATA_CA)
            .withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    @Before
    public void setUp() {
        keyStore.create();
        config = new JSONObject()
                .put("enabled", true)
                .put("hubConnectorEntityId","some-entity-id")
                .put("aggregatedMetadata", new JSONObject()
                    .put("trustAnchorUri", "http://example.com")
                    .put("trustStore", new JSONObject()
                            .put("path",keyStore.getAbsolutePath())
                            .put("password",keyStore.getPassword())
                    )
                ).toString();
    }

    @Test
    public void shouldUseTestTrustStoreWithIntegrationTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToIntegration() throws Exception {

        Certificate testEntryCert =  keyStore.getKeyStore().getCertificate("metadataCA");

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(config, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate("metadataCA");

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias("rootCA")).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias("metadataCA")).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(testEntryCert);
    }

    @Test
    public void shouldUseTrustStoreWithProductionTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToProduction() throws Exception {

        Certificate testEntryCert =  keyStore.getKeyStore().getCertificate("metadataCA");
        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(config, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.PRODUCTION);
        Certificate europeanConfigCert =  europeanIdentityConfiguration.getTrustStore().getCertificate("metadataCA");

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias("rootCA")).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias("metadataCA")).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(testEntryCert);
    }

    @Test
    public void shouldUseTestTrustStoreWithComplianceTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToCompliance() throws Exception {

        Certificate testEntryCert = keyStore.getKeyStore().getCertificate("metadataCA");
        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(config, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.COMPLIANCE_TOOL);
        Certificate europeanConfigCert = europeanIdentityConfiguration.getTrustStore().getCertificate("metadataCA");

        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias("rootCA")).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().containsAlias("metadataCA")).isTrue();
        assertThat(europeanIdentityConfiguration.getTrustStore().size()).isEqualTo(2);
        assertThat(europeanConfigCert).isEqualTo(testEntryCert);
    }
    @Test
    public void shouldReadTrustAnchorAtUriGivenEidasIsEnabledWithHubEnvironmentSetToIntegrationWhenTrustAnchorUriHasBeenSpecified() throws IOException {
        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(config, EuropeanIdentityConfiguration.class);
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);

        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(URI.create("http://example.com"));
    }

    @Test @Ignore
    public void shouldReadTrustAnchorAtUriGivenEidasIsEnabledWithoutHubEnvironmentSetToIntegrationWhenTrustAnchorUriHasBeenSpecified() throws IOException {

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(config, EuropeanIdentityConfiguration.class);

        assertThat(europeanIdentityConfiguration.getTrustAnchorUri()).isEqualTo(URI.create("http://example.com"));
    }

    @Test
    public void shouldReadHubEnvironmentTrustAnchorGivenEidasIsEnabledWithHubEnvironmentSetToIntegrationWhenNoTrustAnchorUriHasBeenSpecified() throws IOException {
        config = new JSONObject()
                .put("enabled", true)
                .put("hubConnectorEntityId","some-entity-id")
                .put("aggregatedMetadata", new JSONObject()
                        .put("trustStore", new JSONObject()
                                .put("path",keyStore.getAbsolutePath())
                                .put("password",keyStore.getPassword())
                        )
                ).toString();
        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(config, EuropeanIdentityConfiguration.class);
        EidasMetadataConfiguration eidasMetadataConfiguration = europeanIdentityConfiguration.getAggregatedMetadata();
        europeanIdentityConfiguration.setEnvironment(HubEnvironment.INTEGRATION);

        assertThat(eidasMetadataConfiguration.getTrustAnchorUri()).isNotEqualTo(URI.create("http://example.com"));
    }


    @Test(expected = NullPointerException.class)
    public void shouldErrorWhenReadingTrustAnchorGivenEidasIsEnabledWithNoSpecifiedTrustAnchorUriAndNoSpecifiedEnvironment() throws IOException {
        config = new JSONObject()
                .put("enabled", true)
                .put("hubConnectorEntityId","some-entity-id")
                .put("aggregatedMetadata", new JSONObject()
                        .put("trustStore", new JSONObject()
                                .put("path",keyStore.getAbsolutePath())
                                .put("password",keyStore.getPassword())
                        )
                ).toString();

        EuropeanIdentityConfiguration europeanIdentityConfiguration = OBJECT_MAPPER.readValue(config, EuropeanIdentityConfiguration.class);
        assertThat(europeanIdentityConfiguration.getTrustAnchorUri());
    }
}
