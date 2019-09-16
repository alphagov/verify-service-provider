package uk.gov.ida.verifyserviceprovider.resources;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import uk.gov.ida.verifyserviceprovider.services.OidcService;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
public class OidcAuthenticationResource {
    private final OidcService oidcService;

    public OidcAuthenticationResource() {
        this.oidcService = new OidcService();
    }

    @GET
    @Path("/getAuthorizationCode")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthorizationCode() {
        // TODO this VSP interface isn't right but this structure helps with testing for the moment. Will need to revisit.
        return getAuthorizationCodeViaHttpRedirect();
    }

    @GET
    @Path("/authenticationRequestCallback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticationRequestCallback(@Context UriInfo uriInfo) {
        try {
            AuthenticationResponse authenticationResponse = AuthenticationResponseParser.parse(uriInfo.getRequestUri());
            if (authenticationResponse instanceof AuthenticationErrorResponse) {
                ErrorObject error = authenticationResponse.toErrorResponse().getErrorObject();
                // TODO handle exceptions
            }

            // TODO validate authentication response

            AuthorizationCode authorizationCode = authenticationResponse.toSuccessResponse().getAuthorizationCode();
            return Response.ok(authorizationCode).build();
        } catch (ParseException e) {
            //TODO handle exceptions
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/getClaims")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClaims(@QueryParam("code") @NotNull AuthorizationCode authorizationCode) {
        try {
            OIDCTokens tokens = oidcService.getTokens(authorizationCode);
            String idTokenSubject = tokens.getIDToken().getJWTClaimsSet().getSubject();

            UserInfo userInfo = oidcService.getUserInfo(tokens.getBearerAccessToken());
            String userInfoSubject = userInfo.getSubject().getValue();

            // TODO design JSON response
            return Response.ok(userInfoSubject).build();
        } catch (java.text.ParseException e) {
            // TODO handle exceptions
            throw new RuntimeException();
        }
    }

    private Response getAuthorizationCodeViaHttpRedirect() {
        return Response
                .status(302)
                .location(oidcService.generateAuthenticationRequest().toURI())
                .build();
    }
}
