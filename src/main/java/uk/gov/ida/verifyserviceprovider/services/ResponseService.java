package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;

import java.util.List;

public class ResponseService<T> {

    private final StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer;
    private final AssertionDecrypter assertionDecrypter;
    private final AssertionService<T> assertionService;
    private final SamlResponseSignatureValidator responseSignatureValidator;
    private final InstantValidator instantValidator;

    public ResponseService(
        StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer,
        AssertionDecrypter assertionDecrypter,
        AssertionService<T> assertionService,
        SamlResponseSignatureValidator responseSignatureValidator,
        InstantValidator instantValidator
    ) {
        this.stringToOpenSamlObjectTransformer = stringToOpenSamlObjectTransformer;
        this.assertionDecrypter = assertionDecrypter;
        this.assertionService = assertionService;
        this.responseSignatureValidator = responseSignatureValidator;
        this.instantValidator = instantValidator;
    }

    public T convertTranslatedResponseBody(
        String decodedSamlResponse,
        String expectedInResponseTo,
        LevelOfAssurance expectedLevelOfAssurance,
        String entityId
    ) {
        Response response = stringToOpenSamlObjectTransformer.apply(decodedSamlResponse);

        ValidatedResponse validatedResponse = responseSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);

        if (!expectedInResponseTo.equals(validatedResponse.getInResponseTo())) {
            throw new SamlResponseValidationException(
                String.format("Expected InResponseTo to be %s, but was %s", expectedInResponseTo, response.getInResponseTo())
            );
        }

        instantValidator.validate(validatedResponse.getIssueInstant(), "Response IssueInstant");

        StatusCode statusCode = validatedResponse.getStatus().getStatusCode();

        switch (statusCode.getValue()) {
            case StatusCode.RESPONDER:
                return assertionService.translateNonSuccessResponse(statusCode);
            case StatusCode.SUCCESS:
                List<Assertion> assertions = assertionDecrypter.decryptAssertions(validatedResponse);
                return assertionService.translateSuccessResponse(assertions, expectedInResponseTo, expectedLevelOfAssurance, entityId);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML status: %s", statusCode.getValue()));
        }
    }

}
