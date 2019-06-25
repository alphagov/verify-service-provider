package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import java.util.List;

public class ClassifyingAssertionService implements AssertionService {

    private final VerifyAssertionTranslator verifyAssertionTranslator;
    private final EidasAssertionTranslator eidasAssertionTranslator;


    public ClassifyingAssertionService(
            VerifyAssertionTranslator verifyAssertionTranslator,
            EidasAssertionTranslator eidasAssertionTranslator
    ) {
        this.verifyAssertionTranslator = verifyAssertionTranslator;
        this.eidasAssertionTranslator = eidasAssertionTranslator;
    }


    @Override
    public TranslatedResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        IdentityAssertionTranslator assertionService = getAssertionService(assertions);

        return assertionService.translateSuccessResponse(assertions, expectedInResponseTo, expectedLevelOfAssurance, entityId);
    }

    private IdentityAssertionTranslator getAssertionService(List<Assertion> assertions) {
        return isEidasIdentity(assertions) ? eidasAssertionTranslator : verifyAssertionTranslator;
    }


    private boolean isEidasIdentity(List<Assertion> assertions) {
        return assertions.stream().anyMatch(eidasAssertionTranslator::isCountryAssertion);
    }

}
