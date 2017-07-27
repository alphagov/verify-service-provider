package uk.gov.ida.verifyserviceprovider.resources;

import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.saml.AuthnRequestFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

@Path("/generate-request")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GenerateAuthnRequestResource {

    private final URI ssoLocation;
    private final AuthnRequestFactory authnRequestFactory;

    public GenerateAuthnRequestResource(AuthnRequestFactory authnRequestFactory, URI ssoLocation) {
        this.authnRequestFactory = authnRequestFactory;
        this.ssoLocation = ssoLocation;
    }

    @POST
    public Response generateAuthnRequest(RequestGenerationBody requestGenerationBody) {
        AuthnRequest authnRequest = this.authnRequestFactory.build(requestGenerationBody.getLevelOfAssurance());
        XmlObjectToBase64EncodedStringTransformer xmlToBase64Transformer = new XmlObjectToBase64EncodedStringTransformer();
        String samlRequest = xmlToBase64Transformer.apply(authnRequest);
        return Response.ok(new RequestResponseBody(samlRequest, authnRequest.getID(), ssoLocation)).build();
    }
}
