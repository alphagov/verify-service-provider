package unit.uk.gov.ida.verifyserviceprovider.configuration;

import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.MetadataUri;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class MetadataUriTest {

    @Test
    public void shouldThrowExceptionWhenMetadataUriIsUnknown() {
        try {
            MetadataUri.fromUri(URI.create("http://example.com"));
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo(
                "Unknown metadata uri: http://example.com. Valid values are: \n" +
                "Production: https://www.signin.service.gov.uk/SAML2/metadata/federation\n" +
                "Integration: https://www.integration.signin.service.gov.uk/SAML2/metadata/federation\n" +
                "Compliance Tool: https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation"
            );
        }

    }
}