package uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.slf4j.LoggerFactory;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;
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
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TranslateSamlResponseResource.class);
    private final EntityIdService entityIdService;


    public TranslateSamlResponseResource(ResponseService responseService, EntityIdService entityIdService) {
        this.responseService = responseService;
        this.entityIdService = entityIdService;
    }

    @POST
    public Response translateResponse(@NotNull @Valid TranslateSamlResponseBody translateSamlResponseBody) throws IOException {
        String entityId = entityIdService.getEntityId(translateSamlResponseBody);
        try {
            TranslatedResponseBody translatedResponseBody = responseService.convertTranslatedResponseBody(
                translateSamlResponseBody.getSamlResponse(),
                translateSamlResponseBody.getRequestId(),
                translateSamlResponseBody.getLevelOfAssurance(),
                entityId
            );

            LOG.info(String.format("Translated response for entityId: %s, requestId: %s, got Scenario: %s",
                    entityId,
                    translateSamlResponseBody.getRequestId(),
                    translatedResponseBody.getScenario()));

            return Response.ok(translatedResponseBody).build();
        } catch (SamlResponseValidationException | SamlTransformationErrorException e) {
            LOG.warn(String.format("Error translating saml response for entityId: %s, requestId: %s, got Message: %s", entityId, translateSamlResponseBody.getRequestId(), e.getMessage()));
            return Response
                .status(BAD_REQUEST)
                .entity(new ErrorMessage(BAD_REQUEST.getStatusCode(), e.getMessage()))
                .build();
        }
    }
}
