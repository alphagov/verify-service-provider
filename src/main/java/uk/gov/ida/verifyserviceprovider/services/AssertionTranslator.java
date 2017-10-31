package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.dto.Scenario.ACCOUNT_CREATION;
import static uk.gov.ida.verifyserviceprovider.dto.Scenario.SUCCESS_MATCH;

public class AssertionTranslator {

    private final SamlAssertionsSignatureValidator assertionsSignatureValidator;
    private final AssertionValidator assertionValidator;

    public AssertionTranslator(
        SamlAssertionsSignatureValidator assertionsSignatureValidator,
        AssertionValidator assertionValidator
    ) {
        this.assertionsSignatureValidator = assertionsSignatureValidator;
        this.assertionValidator = assertionValidator;
    }

    public TranslatedResponseBody translate(
        List<Assertion> assertions,
        String expectedInResponseTo,
        LevelOfAssurance expectedLevelOfAssurance,
        String entityId
    ) {
        validateAssertions(assertions);
        Assertion assertion = assertions.get(0);

        assertionValidator.validate(assertion, expectedInResponseTo, entityId);
        assertionsSignatureValidator.validate(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);

        LevelOfAssurance levelOfAssurance = extractLevelOfAssurance(authnStatement);
        LevelOfAssuranceValidator levelOfAssuranceValidator = new LevelOfAssuranceValidator();
        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);

        String nameID = assertion.getSubject().getNameID().getValue();
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        if (isUserAccountCreation(attributeStatements)) {
            return new TranslatedResponseBody(
                ACCOUNT_CREATION,
                nameID,
                levelOfAssurance,
                AttributeTranslationService.translateAttributes(attributeStatements.get(0))
            );

        }

        return new TranslatedResponseBody(SUCCESS_MATCH, nameID, levelOfAssurance, null);
    }

    public boolean isUserAccountCreation(List<AttributeStatement> attributeStatements) {
        return !attributeStatements.isEmpty();
    }

    private void validateAssertions(List<Assertion> assertions) {
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
