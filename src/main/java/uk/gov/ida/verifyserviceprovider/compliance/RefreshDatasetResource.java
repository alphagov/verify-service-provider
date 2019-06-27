package uk.gov.ida.verifyserviceprovider.compliance;

import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDataset;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/refresh-{a:matching|identity}-dataset")
public class RefreshDatasetResource {
    private final ComplianceToolClient complianceToolClient;

    public RefreshDatasetResource(ComplianceToolClient complianceToolClient) {
        this.complianceToolClient = complianceToolClient;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response execute(@Valid @NotNull MatchingDataset matchingDataset) throws Exception {
        return complianceToolClient.initializeComplianceTool(matchingDataset);

    }

}
