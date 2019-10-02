package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.saml.core.domain.SamlStatusCode;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.dto.MatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import java.util.Optional;

public class MatchingResponderCodeTranslator implements ResponderCodeTranslator {
    @Override
    public TranslatedResponseBody translateResponderCode(StatusCode statusCode) {
        Optional.ofNullable(statusCode.getStatusCode())
            .orElseThrow(() -> new SamlResponseValidationException("Missing status code for non-Success response"));
        String subStatus = statusCode.getStatusCode().getValue();

        switch (subStatus) {
            case SamlStatusCode.NO_MATCH:
                return new TranslatedMatchingResponseBody(MatchingScenario.NO_MATCH, null, null, null);
            case StatusCode.REQUESTER:
                return new TranslatedMatchingResponseBody(MatchingScenario.REQUEST_ERROR, null, null, null);
            case StatusCode.NO_AUTHN_CONTEXT:
                return new TranslatedMatchingResponseBody(MatchingScenario.CANCELLATION, null, null, null);
            case StatusCode.AUTHN_FAILED:
                return new TranslatedMatchingResponseBody(MatchingScenario.AUTHENTICATION_FAILED, null, null, null);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML sub-status: %s", subStatus));
        }
    }
}
