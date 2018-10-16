package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.List;

public class NonMatchingAssertionService implements AssertionService {

    public NonMatchingAssertionService() {
    }

    @Override
    public TranslatedResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        validateAssertions(assertions, expectedInResponseTo, expectedLevelOfAssurance, entityId);
        return translateAssertions(assertions);
    }

    private void validateAssertions(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        throw new SamlResponseValidationException("Oops");
    }

    private TranslatedResponseBody translateAssertions(List<Assertion> assertions) {
        return null;
    }

    @Override
    public TranslatedResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        return null;
    }
}
