package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import java.util.List;

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

    public TranslatedResponseBody convertTranslatedResponseBody(String decodedSamlResponse) {
        Response response = stringToOpenSamlObjectTransformer.apply(decodedSamlResponse);
        ValidatedResponse validatedResponse = responseSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        List<Assertion> assertions = assertionDecrypter.decryptAssertions(validatedResponse);

        return assertionTranslator.translate(assertions);
    }
}
