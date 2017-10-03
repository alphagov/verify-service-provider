package uk.gov.ida.verifyserviceprovider.resources;

import uk.gov.ida.verifyserviceprovider.utils.ManifestReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/version-number")
@Produces(MediaType.TEXT_PLAIN)
public class VersionNumberResource {
    private final ManifestReader manifestReader;

    public VersionNumberResource(ManifestReader manifestReader) {
        this.manifestReader = manifestReader;
    }

    @GET
    public Response getVersionNumber() {
        String version = manifestReader.getVersion();
        return Response.ok(version).build();
    }
}
