package uk.gov.ida.verifyserviceprovider.resources;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.verifyserviceprovider.dto.ErrorBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.format.DateTimeParseException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/translate-response")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranslateSamlResponseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateSamlResponseResource.class);
    private final ResponseService responseService;

    public TranslateSamlResponseResource(ResponseService responseService) {
        this.responseService = responseService;
    }

    @POST
    public Response translateResponse(TranslateSamlResponseBody translateSamlResponseBody) throws Base64DecodingException, IOException {
        try {
            return Response
                .ok(responseService.convertTranslatedResponseBody(translateSamlResponseBody.getSamlResponse()))
                .build();
        } catch (SamlResponseValidationException e) {
            return Response
                    .status(BAD_REQUEST)
                    .entity(new ErrorBody(BAD_REQUEST.name(), e.getMessage()))
                    .build();
        }
    }
}
