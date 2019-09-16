package uk.gov.ida.verifyserviceprovider.services;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import java.io.IOException;
import java.net.URI;

public class OidcService {
    // TODO client registration so that these values are not hardcoded
    private final String providerAuthenticationRequestUri = "http://localhost:50140/barclays/authorize";
    private final String providerTokenRequestUri = "http://localhost:50140/barclays/token";
    private final String providerUserInfoUri = "http://localhost:50140/barclays/userinfo";

    private final String clientAuthenticationRequestRedirectUri = "http://localhost:50400/authenticationRequestCallback";
    private final ClientID clientID = new ClientID("vsp");
    private final Secret clientSecret = new Secret();

    public OidcService() { }

    public OIDCTokens getTokens(AuthorizationCode authorizationCode) {
        TokenRequest tokenRequest = generateTokenRequest(authorizationCode);

        try {
            TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());

            if (!tokenResponse.indicatesSuccess()) {
                ErrorObject error = tokenResponse.toErrorResponse().getErrorObject();
                // TODO handle exceptions
                throw new RuntimeException();
            }

            // TODO validate token response

            return tokenResponse.toSuccessResponse().getTokens().toOIDCTokens();
        } catch (IOException | ParseException e) {
            // TODO handle exceptions
            throw new RuntimeException(e);
        }
    }

    public UserInfo getUserInfo(BearerAccessToken accessToken) {
        UserInfoRequest userInfoRequest = generateUserInfoRequest(accessToken);

        try {
            UserInfoResponse userInfoResponse = UserInfoResponse.parse(userInfoRequest.toHTTPRequest().send());

            if (!userInfoResponse.indicatesSuccess()) {
                ErrorObject error = userInfoResponse.toErrorResponse().getErrorObject();
                // TODO handle exceptions
                throw new RuntimeException();
            }

            // TODO validate userinfo response

            return userInfoResponse.toSuccessResponse().getUserInfo();
        } catch (IOException | ParseException e) {
            // TODO handle exceptions
            throw new RuntimeException(e);
        }
    }

    public AuthenticationRequest generateAuthenticationRequest() {
        // TODO what should these values be?
        State state = new State();
        Nonce nonce = new Nonce();
        Scope scope = new Scope("openid");

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                URI.create(providerAuthenticationRequestUri),
                new ResponseType(ResponseType.Value.CODE),
                scope, clientID, URI.create(clientAuthenticationRequestRedirectUri), state, nonce);

        return authenticationRequest;
    }

    private TokenRequest generateTokenRequest(AuthorizationCode authCode) {
        return new TokenRequest(
                URI.create(providerTokenRequestUri),
                new ClientSecretBasic(clientID, clientSecret),
                new AuthorizationCodeGrant(authCode, URI.create(clientAuthenticationRequestRedirectUri)));
    }

    private UserInfoRequest generateUserInfoRequest(BearerAccessToken accessToken) {
        return new UserInfoRequest(URI.create(providerUserInfoUri), accessToken);
    }
}
