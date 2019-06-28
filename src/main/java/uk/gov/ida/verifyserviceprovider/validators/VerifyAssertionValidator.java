package uk.gov.ida.verifyserviceprovider.validators;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.Optional;

import static java.util.Collections.singletonList;

public class VerifyAssertionValidator {

    private final SamlAssertionsSignatureValidator assertionsSignatureValidator;
    private final AssertionAttributeStatementValidator attributeStatementValidator;
    private final SubjectValidator subjectValidator;

    public VerifyAssertionValidator(
        SamlAssertionsSignatureValidator assertionsSignatureValidator,
        AssertionAttributeStatementValidator attributeStatementValidator,
        SubjectValidator subjectValidator) {
        this.assertionsSignatureValidator = assertionsSignatureValidator;
        this.attributeStatementValidator = attributeStatementValidator;
        this.subjectValidator = subjectValidator;
    }

    public void validate(Assertion assertion, String expectedInResponseTo) {

        if (assertion.getIssueInstant() == null) {
            throw new SamlResponseValidationException("Assertion IssueInstant is missing.");
        }

        if (assertion.getID() == null || assertion.getID().length() == 0) {
            throw new SamlResponseValidationException("Assertion Id is missing or blank.");
        }

        Optional.ofNullable(assertion.getIssuer())
            .map((NameIDType::getValue))
            .filter((value) -> value.length() != 0)
            .orElseThrow(
                () -> new SamlResponseValidationException("Assertion with id " + assertion.getID() + " has missing or blank Issuer.")
            );

        if (assertion.getVersion() == null) {
            throw new SamlResponseValidationException("Assertion with id " + assertion.getID() + " has missing Version.");
        }

        if (!assertion.getVersion().equals(SAMLVersion.VERSION_20)) {
            throw new SamlResponseValidationException("Assertion with id " + assertion.getID() + " declared an illegal Version attribute value.");
        }

        assertionsSignatureValidator.validate(singletonList(assertion), IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        attributeStatementValidator.validate(assertion);
    }
}
