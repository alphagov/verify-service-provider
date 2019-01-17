package uk.gov.ida.verifyserviceprovider.compliance;

import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingDataset;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/refresh-matching-dataset")
public class RefreshDatasetResource {
    private final ComplianceToolService complianceToolService;

    public RefreshDatasetResource(ComplianceToolService complianceToolService) {
        this.complianceToolService = complianceToolService;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response execute(@Valid @NotNull MatchingDataset matchingDataset) throws Exception {
        Response response = complianceToolService.initializeComplianceTool(matchingDataset);
        return response;

    }

}
