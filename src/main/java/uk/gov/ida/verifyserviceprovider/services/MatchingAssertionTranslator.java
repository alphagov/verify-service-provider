package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;

import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.dto.MatchingScenario.ACCOUNT_CREATION;
import static uk.gov.ida.verifyserviceprovider.dto.MatchingScenario.SUCCESS_MATCH;

public class MatchingAssertionTranslator implements AssertionTranslator {


    private AssertionValidator assertionValidator;
    private LevelOfAssuranceValidator levelOfAssuranceValidator;
    private SamlAssertionsSignatureValidator assertionsSignatureValidator;

    public MatchingAssertionTranslator(AssertionValidator assertionValidator,
                                       LevelOfAssuranceValidator levelOfAssuranceValidator,
                                       SamlAssertionsSignatureValidator assertionsSignatureValidator) {
        this.assertionValidator = assertionValidator;
        this.levelOfAssuranceValidator = levelOfAssuranceValidator;
        this.assertionsSignatureValidator = assertionsSignatureValidator;
    }


    @Override
    public TranslatedResponseBody translateSuccessResponse(
            List<Assertion> assertions,
            String expectedInResponseTo,
            LevelOfAssurance expectedLevelOfAssurance,
            String entityId
    ) {
        //  1. check saml has assertions
        checkSamlhasAssertions(assertions);
        //  2. validate assertions
        Assertion assertion = assertions.get(0);
        assertionValidator.validate(assertion, expectedInResponseTo, entityId);
        assertionsSignatureValidator.validate(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        //  3. validate levelOfAssurance
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        LevelOfAssurance levelOfAssurance = extractLevelOfAssurance(authnStatement);
        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);
        //  4. translateAssertions
        String nameID = assertion.getSubject().getNameID().getValue();
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        if (isUserAccountCreation(attributeStatements)) {
            return new TranslatedMatchingResponseBody(
                ACCOUNT_CREATION,
                nameID,
                levelOfAssurance,
                AttributeTranslator.translateAttributes(attributeStatements.get(0))
            );

        }
        return new TranslatedMatchingResponseBody(SUCCESS_MATCH, nameID, levelOfAssurance, null);

    }

    private boolean isUserAccountCreation(List<AttributeStatement> attributeStatements) {
        return !attributeStatements.isEmpty();
    }

    private void checkSamlhasAssertions(List<Assertion> assertions) {
        if (assertions == null || assertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one assertion is expected.");
        }
    }

    private LevelOfAssurance extractLevelOfAssurance(AuthnStatement authnStatement) {
        String levelOfAssuranceString = ofNullable(authnStatement.getAuthnContext())
            .map(AuthnContext::getAuthnContextClassRef)
            .map(AuthnContextClassRef::getAuthnContextClassRef)
            .orElseThrow(() -> new SamlResponseValidationException("Expected a level of assurance."));

        try {
            return LevelOfAssurance.fromSamlValue(levelOfAssuranceString);
        } catch (Exception ex) {
            throw new SamlResponseValidationException(String.format("Level of assurance '%s' is not supported.", levelOfAssuranceString));
        }
    }
}
