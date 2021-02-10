package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import java.util.List;

public class ClassifyingAssertionTranslator implements AssertionTranslator {

    private final VerifyAssertionTranslator verifyAssertionTranslator;


    public ClassifyingAssertionTranslator(
            VerifyAssertionTranslator verifyAssertionTranslator
    ) {
        this.verifyAssertionTranslator = verifyAssertionTranslator;
    }


    @Override
    public TranslatedResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        return verifyAssertionTranslator.translateSuccessResponse(assertions, expectedInResponseTo, expectedLevelOfAssurance, entityId);
    }
}
