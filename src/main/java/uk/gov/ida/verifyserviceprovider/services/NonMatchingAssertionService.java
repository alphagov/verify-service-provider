package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.domain.AssertionData;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier.AssertionType;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_ISSUERS;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_PIDS;

public class NonMatchingAssertionService implements AssertionService {

    private final SamlAssertionsSignatureValidator assertionsSignatureValidator;
    private final SubjectValidator subjectValidator;
    private final AssertionAttributeStatementValidator attributeStatementValidator;
    private final AuthnContextFactory authnContextFactory;
    private final MatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private final AssertionClassifier assertionClassifierService;

    public NonMatchingAssertionService(
            SamlAssertionsSignatureValidator assertionsSignatureValidator,
            SubjectValidator subjectValidator,
            AssertionAttributeStatementValidator attributeStatementValidator,
            AuthnContextFactory authnContextFactory,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            AssertionClassifier assertionClassifierService ) {
        this.assertionsSignatureValidator = assertionsSignatureValidator;
        this.subjectValidator = subjectValidator;
        this.attributeStatementValidator = attributeStatementValidator;
        this.authnContextFactory = authnContextFactory;
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
        this.assertionClassifierService = assertionClassifierService;
    }

    @Override
    public TranslatedResponseBody translateSuccessResponse( List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId ) {
        validate(assertions, expectedInResponseTo, expectedLevelOfAssurance);
        return null;
    }


    public void validate(List<Assertion> assertions, String requestId, LevelOfAssurance expectedLevelOfAssurance ) {

        Map<AssertionType, List<Assertion>> assertionMap = assertions.stream()
                .collect(Collectors.groupingBy(assertionClassifierService::classifyAssertion));

        List<Assertion> authnAssertions = assertionMap.get(AssertionType.AUTHN_ASSERTION);
        if (authnAssertions == null || authnAssertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one authn statement is expected.");
        }

        List<Assertion> mdsAssertions = assertionMap.get(AssertionType.MDS_ASSERTION);
        if (mdsAssertions == null || mdsAssertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one matching dataset assertion is expected.");
        }

        Assertion authnAssertion = authnAssertions.get(0);
        Assertion mdsAssertion = mdsAssertions.get(0);

        validateIdpAssertion(authnAssertion, requestId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        validateIdpAssertion(mdsAssertion, requestId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);


        if (!mdsAssertion.getIssuer().getValue().equals(authnAssertion.getIssuer().getValue())) {
            throw new SamlResponseValidationException(MISMATCHED_ISSUERS);
        }

        if (!mdsAssertion.getSubject().getNameID().getValue().equals(authnAssertion.getSubject().getNameID().getValue())) {
            throw new SamlResponseValidationException(MISMATCHED_PIDS);
        }
    }

    public void validateIdpAssertion( Assertion assertion,
                                      String expectedInResponseTo,
                                      QName role ) {

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
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        attributeStatementValidator.validate(assertion);
    }


    @Override
    public TranslatedResponseBody translateNonSuccessResponse( StatusCode statusCode ) {
        return null;
    }

    public AssertionData translate(List<Assertion> assertions ) {
        Map<AssertionType, List<Assertion>> assertionMap = assertions.stream()
                .collect(Collectors.groupingBy(assertionClassifierService::classifyAssertion));

        AuthnStatement authnStatement = assertionMap.get(AssertionType.AUTHN_ASSERTION).get(0).getAuthnStatements().get(0);
        String levelOfAssurance = authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
        Assertion mdsAssertion = assertionMap.get(AssertionType.MDS_ASSERTION).get(0);
        Issuer issuer = mdsAssertion.getIssuer();
        return new AssertionData(issuer.getValue(),
                authnContextFactory.authnContextForLevelOfAssurance(levelOfAssurance),
                matchingDatasetUnmarshaller.fromAssertion(mdsAssertion));
    }
}
