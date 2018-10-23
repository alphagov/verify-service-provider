package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.saml.core.transformers.inbound.Cycle3DatasetFactory;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.*;
import javax.xml.namespace.QName;
import java.util.List;

import static java.util.Collections.singletonList;

public abstract class AssertionService {

    final AssertionValidator assertionValidator;

    final SamlAssertionsSignatureValidator assertionsSignatureValidator;

    private final AssertionAttributeStatementValidator attributeStatementValidator;


    public AssertionService(SamlAssertionsSignatureValidator assertionsSignatureValidator,
                            AssertionValidator assertionValidator)
    {
        this.assertionsSignatureValidator = assertionsSignatureValidator;

        this.assertionValidator = assertionValidator;

        this.attributeStatementValidator = new AssertionAttributeStatementValidator();
    }

    protected abstract TranslatedResponseBody translateSuccessResponse(
            List<Assertion> assertions,
            String expectedInResponseTo,
            LevelOfAssurance expectedLevelOfAssurance,
            String entityId
    );

    protected abstract TranslatedResponseBody translateNonSuccessResponse(StatusCode statusCode);

    public void validateIdPAssertion(Assertion assertion,
                                     String expectedInResponseTo,
                                     QName role) {

        if (assertion.getIssueInstant() == null) {
            throw new SamlResponseValidationException("Assertion IssueInstant is missing.");
        }

        if (assertion.getID() == null || assertion.getID().length() == 0) {
            throw new SamlResponseValidationException("Assertion Id is missing or blank.");
        }

        if (assertion.getIssuer() == null || assertion.getIssuer().getValue() == null || assertion.getIssuer().getValue().length() == 0) {
            throw new SamlResponseValidationException("Assertion with id " + assertion.getID() + " has missing or blank Issuer.");
        }

        if (assertion.getVersion() == null) {
            throw new SamlResponseValidationException("Assertion with id " + assertion.getID() + " has missing Version.");
        }

        if (!assertion.getVersion().equals(SAMLVersion.VERSION_20)) {
            throw new SamlResponseValidationException("Assertion with id " + assertion.getID() + " declared an illegal Version attribute value.");
        }

        assertionsSignatureValidator.validate(singletonList(assertion), role);
        assertionValidator.getSubjectValidator().validate(assertion.getSubject(), expectedInResponseTo);
        attributeStatementValidator.validate(assertion);
    }

}
