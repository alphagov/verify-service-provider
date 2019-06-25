package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import java.util.List;

public class SingleAssertionService extends IdentityAssertionService {
    private final IdentityAssertionTranslator assertionService;

    public SingleAssertionService(IdentityAssertionTranslator assertionService) {
        this.assertionService = assertionService;
    }

    @Override
    public TranslatedResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        return assertionService.translateSuccessResponse(assertions, expectedInResponseTo, expectedLevelOfAssurance, entityId);
    }
}
