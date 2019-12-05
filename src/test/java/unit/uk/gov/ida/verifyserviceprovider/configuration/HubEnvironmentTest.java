package unit.uk.gov.ida.verifyserviceprovider.configuration;

import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.HubEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class HubEnvironmentTest {
    @Test
    public void shouldThrowExceptionWhenEnvironmentIsUnknown() {
        try {
            HubEnvironment.fromString("NOT_AN_ENVIRONMENT");
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo(
                "Unrecognised Hub Environment: NOT_AN_ENVIRONMENT. \n" +
                "Valid values are: PRODUCTION, INTEGRATION, COMPLIANCE_TOOL"
            );
        }
    }
}
