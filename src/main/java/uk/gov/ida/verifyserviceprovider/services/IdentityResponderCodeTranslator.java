package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.verifyserviceprovider.dto.IdentityScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedIdentityResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.Optional;

public class IdentityResponderCodeTranslator implements ResponderCodeTranslator {
    @Override
    public TranslatedIdentityResponseBody translateResponderCode(StatusCode statusCode) {
        Optional.ofNullable(statusCode.getStatusCode())
            .orElseThrow(() -> new SamlResponseValidationException("Missing status code for non-Success response"));
        String subStatus = statusCode.getStatusCode().getValue();

        switch (subStatus) {
            case StatusCode.REQUESTER:
                return new TranslatedIdentityResponseBody(IdentityScenario.REQUEST_ERROR, null, null, null);
            case StatusCode.NO_AUTHN_CONTEXT:
                return new TranslatedIdentityResponseBody(IdentityScenario.NO_AUTHENTICATION, null, null, null);
            case StatusCode.AUTHN_FAILED:
                return new TranslatedIdentityResponseBody(IdentityScenario.AUTHENTICATION_FAILED, null, null, null);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML sub-status: %s", subStatus));
        }
    }

}
