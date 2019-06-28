package uk.gov.ida.verifyserviceprovider.validators;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.List;

import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_ISSUERS;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_PIDS;

public class VerifyAssertionsValidator extends IdentityAssertionValidator {
    private final VerifyAssertionValidator verifyAssertionValidator;
    public VerifyAssertionsValidator(VerifyAssertionValidator verifyAssertionValidator) {
        this.verifyAssertionValidator = verifyAssertionValidator;
    }

    @Override
    public void validate(List<Assertion> assertions,
                         String requestId) {
        if (assertions.size() != 2) {
            throw new SamlResponseValidationException("Two assertions are expected from a Verify IDP.");
        }

        Assertion leftAssertion = assertions.get(0);
        Assertion rightAssertion = assertions.get(1);
        verifyAssertionValidator.validate(leftAssertion, requestId);
        verifyAssertionValidator.validate(rightAssertion, requestId);

        if (!getIssuerValue(rightAssertion).equals(getIssuerValue(leftAssertion))) {
            throw new SamlResponseValidationException(MISMATCHED_ISSUERS);
        }

        if (!getSubjectValue(leftAssertion).equals(getSubjectValue(rightAssertion))) {
            throw new SamlResponseValidationException(MISMATCHED_PIDS);
        }
    }

    private String getIssuerValue(Assertion assertion) {
        return assertion.getIssuer().getValue();
    }

    private String getSubjectValue(Assertion assertion) {
        return assertion.getSubject().getNameID().getValue();
    }

}
