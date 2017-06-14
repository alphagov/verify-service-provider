package uk.gov.ida.verifyserviceprovider.resources;

import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslateResponseRequestBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

@Path("/translate-response")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranslateResponseResource {

    @POST
    public Response translateResponse(TranslateResponseRequestBody request) {
        LevelOfAssurance levelOfAssurance = request.authnResponse.contains("LEVEL_1") ? LevelOfAssurance.LEVEL_1 : LevelOfAssurance.LEVEL_2;
        return Response.ok(new TranslatedResponseBody("pid", levelOfAssurance, Collections.EMPTY_LIST)).build();
    }
}
