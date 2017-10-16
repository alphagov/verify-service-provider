package unit.uk.gov.ida.verifyserviceprovider.configuration;

import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.MatchingServiceAdapterConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.MSA_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class MatchingServiceAdapterConfigurationTest {
    @Test
    public void shouldSetDefaultConfigValuesWhenNotProvided() throws Exception {
        String configurationAsString = "{\"metadataUri\": \"http://some-msa-uri\"}";

        MatchingServiceAdapterConfiguration actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MatchingServiceAdapterConfiguration.class);

        assertThat(actualConfiguration.getMinRefreshDelay()).isEqualTo(60000);
        assertThat(actualConfiguration.getMaxRefreshDelay()).isEqualTo(600000);
        assertThat(actualConfiguration.getJerseyClientConfiguration()).isNotNull();
        assertThat(actualConfiguration.getJerseyClientName()).isEqualTo(MSA_JERSEY_CLIENT_NAME);
    }
}