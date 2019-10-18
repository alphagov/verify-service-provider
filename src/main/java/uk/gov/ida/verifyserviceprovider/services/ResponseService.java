package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.MissingUnsignedAssertionsHandlerException;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;

import java.util.List;

public class ResponseService {

    private final int ONLY_ONE_PRESENT = 0;
    private final StringToOpenSamlObjectTransformer<Response> samlObjectTransformer;
    private final AssertionDecrypter assertionDecrypter;
    private final AssertionTranslator assertionTranslator;
    private final SamlResponseSignatureValidator responseSignatureValidator;
    private final InstantValidator instantValidator;
    private final ResponderCodeTranslator responderCodeTranslator;
    private final UnsignedAssertionsResponseHandler unsignedAssertionsResponseHandler;

    public ResponseService(
            StringToOpenSamlObjectTransformer<Response> samlObjectTransformer,
            AssertionDecrypter assertionDecrypter,
            AssertionTranslator assertionTranslator,
            SamlResponseSignatureValidator responseSignatureValidator,
            InstantValidator instantValidator,
            ResponderCodeTranslator responderCodeTranslator,
            UnsignedAssertionsResponseHandler unsignedAssertionsResponseHandler
    ) {
        this.samlObjectTransformer = samlObjectTransformer;
        this.assertionDecrypter = assertionDecrypter;
        this.assertionTranslator = assertionTranslator;
        this.responseSignatureValidator = responseSignatureValidator;
        this.instantValidator = instantValidator;
        this.responderCodeTranslator = responderCodeTranslator;
        this.unsignedAssertionsResponseHandler = unsignedAssertionsResponseHandler;
    }

    public TranslatedResponseBody convertTranslatedResponseBody(
        String decodedSamlResponse,
        String expectedInResponseTo,
        LevelOfAssurance expectedLevelOfAssurance,
        String entityId
    ) {
        Response response = samlObjectTransformer.apply(decodedSamlResponse);
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
                return responderCodeTranslator.translateResponderCode(statusCode);
            case StatusCode.SUCCESS:
                List<Assertion> assertions = assertionDecrypter.decryptAssertions(validatedResponse);
                if (assertionsContainEidasUnsignedAssertionsResponse(assertions)) {
                    if (unsignedAssertionsResponseHandler == null) { throw new MissingUnsignedAssertionsHandlerException(); }

                    ValidatedResponse validatedCountryResponse = unsignedAssertionsResponseHandler.getValidatedResponse(assertions, expectedInResponseTo);
                    assertions = unsignedAssertionsResponseHandler.decryptAssertion(validatedCountryResponse, assertions.get(ONLY_ONE_PRESENT));
                }
                return assertionTranslator.translateSuccessResponse(assertions, expectedInResponseTo, expectedLevelOfAssurance, entityId);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML status: %s", statusCode.getValue()));
        }
    }

    private boolean assertionsContainEidasUnsignedAssertionsResponse(List<Assertion> assertions) {
        if (assertions == null || assertions.size() != 1) { return false; }

        List<AttributeStatement> attributeStatements = assertions.get(ONLY_ONE_PRESENT).getAttributeStatements();
        if (attributeStatements.isEmpty() || attributeStatements.size() != 1) { return false; }

        return attributeStatements.get(ONLY_ONE_PRESENT).getAttributes()
                .stream()
                .anyMatch(
                        attribute -> attribute.getName().equals(
                                IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME
                        )
                );
    }
}
