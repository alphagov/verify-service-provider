package uk.gov.ida.verifyserviceprovider.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Path("/translate-response")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranslateSamlResponseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranslateSamlResponseResource.class);

    @POST
    public Response translateResponse(TranslateSamlResponseBody translateSamlResponseBody) {
        String decodedSamlResponse = new String(Base64.getDecoder().decode(translateSamlResponseBody.response));

        try {
            Map<String, Object> samlResponseBody = convertToMap(decodedSamlResponse);
            Optional<String> loa = Optional.ofNullable(samlResponseBody.get("levelOfAssurance"));

            return Response.ok(new TranslatedResponseBody(
                samlResponseBody.get("pid"),
                LevelOfAssurance.valueOf(loa.get()),
                Collections.EMPTY_LIST)
            ).build();
        } catch (IllegalArgumentException | NoSuchElementException | IOException e) {
            LOGGER.error("Error during SAML response translation.", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private Map<String, Object> convertToMap(String decodedSamlResponse) throws IOException {
        return new ObjectMapper().readValue(decodedSamlResponse, new TypeReference<Map<String, Object>>() {
        });
    }
}
