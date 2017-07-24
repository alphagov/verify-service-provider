package unit.uk.gov.ida.verifyserviceprovider.configuration;

import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.MetadataConfigurationWithHubDefaults;
import uk.gov.ida.verifyserviceprovider.configuration.MetadataConfigurationWithMsaDefaults;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.COMPLIANCE_TOOL_METADATA_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.INTEGRATION_METADATA_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.MSA_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_METADATA_URI;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_VERIFY_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_VERIFY_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class MetadataConfigurationWithDefaultsTest {

    @Test
    public void shouldSetHubMetadataTrustStorePathToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String configurationAsString = "{" +
            "\"uri\": \"" + PRODUCTION_METADATA_URI + "\"," +
            "\"trustStorePath\":\"some-truststore-path\"" +
            "}";

        MetadataConfigurationWithHubDefaults actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MetadataConfigurationWithHubDefaults.class);

        assertThat(actualConfiguration.getTrustStorePath()).isEqualTo("some-truststore-path");
    }

    @Test
    public void shouldSetHubMetadataTrustStorePathToProductionWhenUsingProductionMetadata() throws Exception {
        String configurationAsString = "{\"uri\": \"" + PRODUCTION_METADATA_URI + "\"}";

        MetadataConfigurationWithHubDefaults actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MetadataConfigurationWithHubDefaults.class);

        assertThat(actualConfiguration.getTrustStorePath()).endsWith(PRODUCTION_VERIFY_TRUSTSTORE_NAME);
    }

    @Test
    public void shouldSetHubMetadataTrustStorePathToTestWhenUsingIntegrationMetadata() throws Exception {
        String configurationAsString = "{\"uri\": \"" + INTEGRATION_METADATA_URI + "\"}";

        MetadataConfigurationWithHubDefaults actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MetadataConfigurationWithHubDefaults.class);

        assertThat(actualConfiguration.getTrustStorePath()).endsWith(TEST_VERIFY_TRUSTSTORE_NAME);
    }

    @Test
    public void shouldSetHubMetadataTrustStorePathToTestWhenUsingComplianceToolMetadata() throws Exception {
        String configurationAsString = "{\"uri\": \"" + COMPLIANCE_TOOL_METADATA_URI + "\"}";

        MetadataConfigurationWithHubDefaults actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MetadataConfigurationWithHubDefaults.class);

        assertThat(actualConfiguration.getTrustStorePath()).endsWith(TEST_VERIFY_TRUSTSTORE_NAME);
    }

    @Test
    public void shouldSetHubMetadataExpectedEntityIdToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String configurationAsString = "{" +
            "\"uri\": \"" + PRODUCTION_METADATA_URI + "\"," +
            "\"expectedEntityId\":\"some-expected-entity-id\"}";
        MetadataConfigurationWithHubDefaults actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MetadataConfigurationWithHubDefaults.class);

        assertThat(actualConfiguration.getExpectedEntityId()).isEqualTo("some-expected-entity-id");
    }

    @Test
    public void shouldSetHubMetadataExpectedEntityIdToProductionWhenUsingProductionMetadata() throws Exception {
        String configurationAsString = "{\"uri\": \"" + PRODUCTION_METADATA_URI + "\"}";

        MetadataConfigurationWithHubDefaults actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MetadataConfigurationWithHubDefaults.class);

        assertThat(actualConfiguration.getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
    }

    @Test
    public void shouldSetHubMetadataDefaultConfigValuesWhenNotProvided() throws Exception{
        String configurationAsString = "{\"uri\": \"" + PRODUCTION_METADATA_URI + "\"}";

        MetadataConfigurationWithHubDefaults actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MetadataConfigurationWithHubDefaults.class);

        assertThat(actualConfiguration.getTrustStorePassword()).isEqualTo("");
        assertThat(actualConfiguration.getMinRefreshDelay()).isEqualTo(60000);
        assertThat(actualConfiguration.getMaxRefreshDelay()).isEqualTo(600000);
        assertThat(actualConfiguration.getJerseyClientConfiguration()).isNotNull();
        assertThat(actualConfiguration.getJerseyClientName()).isEqualTo(HUB_JERSEY_CLIENT_NAME);
    }

    @Test
    public void shouldSetMsaMetadataDefaultConfigValuesWhenNotProvided() throws Exception {
        String configurationAsString = "{\"uri\": \"" + PRODUCTION_METADATA_URI + "\"}";

        MetadataConfigurationWithMsaDefaults actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MetadataConfigurationWithMsaDefaults.class);

        assertThat(actualConfiguration.getMinRefreshDelay()).isEqualTo(60000);
        assertThat(actualConfiguration.getMaxRefreshDelay()).isEqualTo(600000);
        assertThat(actualConfiguration.getJerseyClientConfiguration()).isNotNull();
        assertThat(actualConfiguration.getJerseyClientName()).isEqualTo(MSA_JERSEY_CLIENT_NAME);
    }
}