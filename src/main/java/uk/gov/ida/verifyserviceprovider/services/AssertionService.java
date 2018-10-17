package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
//import uk.gov.ida.saml.core.
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

   /* protected final InstantValidator instantValidator;
    protected final SubjectValidator subjectValidator;
    protected final ConditionsValidator conditionsValidator;
    //private final SamlAssertionsSignatureValidator hubSignatureValidator;
   //private final AssertionAttributeStatementValidator attributeStatementValidator; */

    protected abstract TranslatedResponseBody translateSuccessResponse(
            List<Assertion> assertions,
            String expectedInResponseTo,
            LevelOfAssurance expectedLevelOfAssurance,
            String entityId
    );

    protected abstract TranslatedResponseBody translateNonSuccessResponse(StatusCode statusCode);

    protected void validateHubAssertion(Assertion assertion,
                                        //String expectedInResponseTo,
                                        String hubEntityId,
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

        /**hubSignatureValidator.validate(singletonList(assertion), role);
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        attributeStatementValidator.validate(assertion);*/
    }
}
