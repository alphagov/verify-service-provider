package unit.uk.gov.ida.verifyserviceprovider.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.verifyserviceprovider.configuration.MsaMetadataConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.MSA_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class MsaMetadataConfigurationTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldSetDefaultConfigValuesWhenNotProvided() throws Exception {
        String configurationAsString = "{\"uri\": \"http://some-msa-uri\", \"expectedEntityId\": \"foo\"}";

        MsaMetadataConfiguration actualConfiguration = OBJECT_MAPPER.readValue(configurationAsString, MsaMetadataConfiguration.class);

        assertThat(actualConfiguration.getMinRefreshDelay()).isEqualTo(60000);
        assertThat(actualConfiguration.getMaxRefreshDelay()).isEqualTo(600000);
        assertThat(actualConfiguration.getJerseyClientConfiguration()).isNotNull();
        assertThat(actualConfiguration.getJerseyClientName()).isEqualTo(MSA_JERSEY_CLIENT_NAME);
    }

    @Test
    public void shouldNotAllowEmptyExpectedEntityId() throws Exception {
        expectedException.expectMessage("Missing required creator property 'expectedEntityId'");
        String configurationAsString = "{\"uri\": \"http://some-msa-uri\"}";

        OBJECT_MAPPER.readValue(configurationAsString, MsaMetadataConfiguration.class);
    }
}