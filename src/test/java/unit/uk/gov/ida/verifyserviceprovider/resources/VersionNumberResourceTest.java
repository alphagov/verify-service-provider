package unit.uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.resources.VersionNumberResource;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VersionNumberResourceTest {
    private static final ManifestReader manifestReader = mock(ManifestReader.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addProvider(JerseyViolationExceptionMapper.class)
        .addProvider(JsonProcessingExceptionMapper.class)
        .addResource(new VersionNumberResource(manifestReader))
        .build();

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
        reset(manifestReader);
    }

    @Test
    public void returnsAnOKResponseWithVersionNumber() throws IOException {
        String versionNumber = "1.2.0";
        when(manifestReader.getAttributeValueFor(VerifyServiceProviderApplication.class, "Version")).thenReturn(versionNumber);

        Response response = resources.target("/version-number").request().get();

        verify(manifestReader, times(1)).getAttributeValueFor(VerifyServiceProviderApplication.class, "Version");
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.readEntity(String.class)).isEqualTo(versionNumber);
    }

    @Test
    public void returns500WhenManifestReaderThrowsException() throws IOException {
        doThrow(new RuntimeException("exception")).when(manifestReader).getAttributeValueFor(VerifyServiceProviderApplication.class, "Version");

        Response response = resources.target("/version-number").request().get();

        verify(manifestReader, times(1)).getAttributeValueFor(VerifyServiceProviderApplication.class, "Version");
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
