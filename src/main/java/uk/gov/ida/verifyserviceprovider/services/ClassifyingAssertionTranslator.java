package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import java.util.List;

public class ClassifyingAssertionTranslator implements AssertionTranslator {

    private final VerifyAssertionTranslator verifyAssertionTranslator;
    private final EidasAssertionTranslator eidasAssertionTranslator;
    private final EidasUnsignedAssertionTranslator eidasUnsignedAssertionTranslator;


    public ClassifyingAssertionTranslator(
            VerifyAssertionTranslator verifyAssertionTranslator,
            EidasAssertionTranslator eidasAssertionTranslator,
            EidasUnsignedAssertionTranslator eidasUnsignedAssertionTranslator
    ) {
        this.verifyAssertionTranslator = verifyAssertionTranslator;
        this.eidasAssertionTranslator = eidasAssertionTranslator;
        this.eidasUnsignedAssertionTranslator = eidasUnsignedAssertionTranslator;
    }


    @Override
    public TranslatedResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        IdentityAssertionTranslator assertionService = getAssertionService(assertions);

        return assertionService.translateSuccessResponse(assertions, expectedInResponseTo, expectedLevelOfAssurance, entityId);
    }

    private IdentityAssertionTranslator getAssertionService(List<Assertion> assertions) {
        return isEidasIdentity(assertions) ? getEidasAssertionService(assertions) : verifyAssertionTranslator;
    }

    private IdentityAssertionTranslator getEidasAssertionService(List<Assertion> assertions) {
        return assertions.get(0).getSignature() == null ? eidasUnsignedAssertionTranslator : eidasAssertionTranslator;
    }

    private boolean isEidasIdentity(List<Assertion> assertions) {
        return assertions.stream().anyMatch(eidasAssertionTranslator::isCountryAssertion);
    }

}
