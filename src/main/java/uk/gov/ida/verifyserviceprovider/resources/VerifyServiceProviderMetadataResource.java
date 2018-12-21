package uk.gov.ida.verifyserviceprovider.resources;

import org.w3c.dom.Document;
import uk.gov.ida.verifyserviceprovider.metadata.MetadataRepository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/metadata")
@Produces("application/samlmetadata+xml")
public class VerifyServiceProviderMetadataResource {

    private final MetadataRepository metadataRepository;

    public VerifyServiceProviderMetadataResource(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @GET
    public Document getMetadata() {
        return metadataRepository.getVerifyServiceProviderMetadata();
    }
}
