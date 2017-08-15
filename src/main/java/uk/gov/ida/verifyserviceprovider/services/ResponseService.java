package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.saml.core.domain.SamlStatusCode;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.Scenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.List;
import java.util.Optional;

public class ResponseService {

    private final StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer;
    private final AssertionDecrypter assertionDecrypter;
    private final AssertionTranslator assertionTranslator;
    private final SamlResponseSignatureValidator responseSignatureValidator;

    public ResponseService(
            StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer,
            AssertionDecrypter assertionDecrypter,
            AssertionTranslator assertionTranslator,
            SamlResponseSignatureValidator responseSignatureValidator) {
        this.stringToOpenSamlObjectTransformer = stringToOpenSamlObjectTransformer;
        this.assertionDecrypter = assertionDecrypter;
        this.assertionTranslator = assertionTranslator;
        this.responseSignatureValidator = responseSignatureValidator;
    }

    public TranslatedResponseBody convertTranslatedResponseBody(String decodedSamlResponse, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance) {
        Response response = stringToOpenSamlObjectTransformer.apply(decodedSamlResponse);

        ValidatedResponse validatedResponse = responseSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        if (!expectedInResponseTo.equals(validatedResponse.getInResponseTo())) {
            throw new SamlResponseValidationException(
                    "Expected InResponseTo to be " + expectedInResponseTo + ", but was " + response.getInResponseTo());
        }

        StatusCode statusCode = validatedResponse.getStatus().getStatusCode();

        switch (statusCode.getValue()) {
            case StatusCode.RESPONDER:
                return translateNonSuccessResponse(statusCode);
            case StatusCode.SUCCESS:
                List<Assertion> assertions = assertionDecrypter.decryptAssertions(validatedResponse);
                return assertionTranslator.translate(assertions, expectedInResponseTo, expectedLevelOfAssurance);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML status: %s", statusCode.getValue()));
        }
    }

    private TranslatedResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        Optional.ofNullable(statusCode.getStatusCode())
                .orElseThrow(() -> new SamlResponseValidationException("Missing status code for non-Success response"));
        String subStatus = statusCode.getStatusCode().getValue();

        switch (subStatus) {
            case SamlStatusCode.NO_MATCH:
                return new TranslatedResponseBody(Scenario.NO_MATCH, null, null, null);
            case StatusCode.REQUESTER:
                return new TranslatedResponseBody(Scenario.REQUEST_ERROR, null, null, null);
            case StatusCode.NO_AUTHN_CONTEXT:
                return new TranslatedResponseBody(Scenario.CANCELLATION, null, null, null);
            case StatusCode.AUTHN_FAILED:
                return new TranslatedResponseBody(Scenario.AUTHENTICATION_FAILED, null, null, null);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML sub-status: %s", subStatus));
        }
    }
}
