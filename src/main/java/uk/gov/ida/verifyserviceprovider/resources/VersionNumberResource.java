package uk.gov.ida.verifyserviceprovider.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/version-number")
@Produces(MediaType.TEXT_PLAIN)
public class VersionNumberResource {
    private static final Logger LOG = LoggerFactory.getLogger(VersionNumberResource.class);

    private final ManifestReader manifestReader;

    public VersionNumberResource(ManifestReader manifestReader) {
        this.manifestReader = manifestReader;
    }

    @GET
    public Response getVersionNumber() {
        String version = "UNKNOWN_VERSION";
        try {
            version = manifestReader.getAttributeValueFor(VerifyServiceProviderApplication.class, "Version");
        } catch (IOException e) {
            LOG.error("Failed to read version number from the manifest", e);
        }

        return Response.ok(version).build();
    }
}
