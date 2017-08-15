package uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/translate-response")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranslateSamlResponseResource {

    private final ResponseService responseService;

    public TranslateSamlResponseResource(ResponseService responseService) {
        this.responseService = responseService;
    }

    @POST
    public Response translateResponse(@NotNull @Valid TranslateSamlResponseBody translateSamlResponseBody) throws IOException {
        try {
            return Response
                .ok(responseService.convertTranslatedResponseBody(
                    translateSamlResponseBody.getSamlResponse(),
                    translateSamlResponseBody.getRequestId(),
                    translateSamlResponseBody.getLevelOfAssurance())
                )
                .build();
        } catch (SamlResponseValidationException | SamlTransformationErrorException e) {
            return Response
                .status(BAD_REQUEST)
                .entity(new ErrorMessage(BAD_REQUEST.getStatusCode(), e.getMessage()))
                .build();
        }
    }
}
