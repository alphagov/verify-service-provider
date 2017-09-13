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
    private final InstantValidator instantValidator;
    private final SubjectValidator subjectValidator;
    private final ConditionsValidator conditionsValidator;

    public AssertionTranslator(SamlAssertionsSignatureValidator assertionsSignatureValidator,
                               InstantValidator instantValidator,
                               SubjectValidator subjectValidator,
                               ConditionsValidator conditionsValidator) {
        this.assertionsSignatureValidator = assertionsSignatureValidator;
        this.instantValidator = instantValidator;
        this.subjectValidator = subjectValidator;
        this.conditionsValidator = conditionsValidator;
    }

    public TranslatedResponseBody translate(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance) {
        Assertion assertion = getAssertion(assertions);

        instantValidator.validate(assertion.getIssueInstant(), "Assertion IssueInstant");

        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        String nameID = assertion.getSubject().getNameID().getValue();

        conditionsValidator.validate(assertion.getConditions());

        assertionsSignatureValidator.validate(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        AuthnStatement authnStatement = getAuthnStatement(assertion);

        instantValidator.validate(authnStatement.getAuthnInstant(), "Assertion AuthnInstant");

        LevelOfAssurance levelOfAssurance = getLevelOfAssurance(authnStatement);
        LevelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);

        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        if (attributeStatements.isEmpty()) {
            return new TranslatedResponseBody(
                SUCCESS_MATCH,
                nameID,
                levelOfAssurance,
                null
            );
        } else {
            // Assume it is user account creation
            return new TranslatedResponseBody(
                ACCOUNT_CREATION,
                nameID,
                levelOfAssurance,
                AttributeTranslationService.translateAttributes(attributeStatements.get(0))
            );
        }
    }


    private AuthnStatement getAuthnStatement(Assertion assertion) {
        List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
        if (authnStatements == null || authnStatements.size() != 1) {
            throw new SamlResponseValidationException("Exactly one authn statement is expected.");
        }
        return authnStatements.get(0);
    }

    private Assertion getAssertion(List<Assertion> assertions) {
        if (assertions == null || assertions.isEmpty() || assertions.size() > 1) {
            throw new SamlResponseValidationException("Exactly one assertion is expected.");
        }
        return assertions.get(0);
    }

    private LevelOfAssurance getLevelOfAssurance(AuthnStatement authnStatement) {
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
