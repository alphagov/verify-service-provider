package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.Optional;

public abstract class IdentityAssertionService implements AssertionService {
    @Override
    public TranslatedNonMatchingResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        Optional.ofNullable(statusCode.getStatusCode())
            .orElseThrow(() -> new SamlResponseValidationException("Missing status code for non-Success response"));
        String subStatus = statusCode.getStatusCode().getValue();

        switch (subStatus) {
            case StatusCode.REQUESTER:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.REQUEST_ERROR, null, null, null);
            case StatusCode.NO_AUTHN_CONTEXT:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.NO_AUTHENTICATION, null, null, null);
            case StatusCode.AUTHN_FAILED:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.AUTHENTICATION_FAILED, null, null, null);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML sub-status: %s", subStatus));
        }
    }
}
