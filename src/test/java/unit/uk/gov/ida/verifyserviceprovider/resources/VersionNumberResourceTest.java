package unit.uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.resources.VersionNumberResource;
import uk.gov.ida.verifyserviceprovider.utils.ManifestReader;

import javax.ws.rs.core.Response;
import java.util.jar.Attributes;

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
    public void returnsAnOKResponseWithVersionNumber() {
        Attributes attributes = mock(Attributes.class);
        String versionNumber = "1.2.0";
        when(attributes.getValue("Version")).thenReturn(versionNumber);
        when(manifestReader.getManifest()).thenReturn(attributes);

        Response response = resources.target("/version-number").request().get();

        verify(manifestReader, times(1)).getManifest();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.readEntity(String.class)).isEqualTo(versionNumber);
    }

    @Test
    public void returns500WhenManifestReaderThrowsException() {
        doThrow(new RuntimeException("exception")).when(manifestReader).getManifest();

        Response response = resources.target("/version-number").request().get();

        verify(manifestReader, times(1)).getManifest();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
