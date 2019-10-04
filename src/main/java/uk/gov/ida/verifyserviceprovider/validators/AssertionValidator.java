package uk.gov.ida.verifyserviceprovider.validators;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;

import java.util.List;

public class AssertionValidator {

    private final InstantValidator instantValidator;
    private final SubjectValidator subjectValidator;
    private final ConditionsValidator conditionsValidator;


    public AssertionValidator(
        InstantValidator instantValidator,
        SubjectValidator subjectValidator,
        ConditionsValidator conditionsValidator
    ) {
        this.instantValidator = instantValidator;
        this.subjectValidator = subjectValidator;
        this.conditionsValidator = conditionsValidator;
    }

    public void validate(Assertion assertion, String expectedInResponseTo, String entityId) {
        instantValidator.validate(assertion.getIssueInstant(), "Assertion IssueInstant");
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        conditionsValidator.validate(assertion.getConditions(), entityId);

        validateAuthnStatements(assertion.getAuthnStatements());

        instantValidator.validate(assertion.getAuthnStatements().get(0).getAuthnInstant(), "Assertion AuthnInstant");
    }

    private void validateAuthnStatements(List<AuthnStatement> authnStatements) {
        if (authnStatements == null || authnStatements.size() != 1) {
            throw new SamlResponseValidationException("Exactly one authn statement is expected.");
        }
    }
}
