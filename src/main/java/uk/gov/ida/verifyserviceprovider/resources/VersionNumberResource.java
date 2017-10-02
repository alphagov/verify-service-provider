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

    public static final String UNKNOWN_VERSION = "UNKNOWN_VERSION";
    private final ManifestReader manifestReader;

    public VersionNumberResource(ManifestReader manifestReader) {
        this.manifestReader = manifestReader;
    }

    @GET
    public Response getVersionNumber() {
        String version = manifestReader.getManifest().map(x -> x.getValue("Version")).orElse(UNKNOWN_VERSION);
        return Response.ok(version).build();
    }
}
