package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_ISSUERS;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_PIDS;

public class NonMatchingAssertionService extends AssertionService {

    private enum AssertionType {AUTHN_ASSERTION, MDS_ASSERTION}

    public NonMatchingAssertionService(SamlAssertionsSignatureValidator assertionsSignatureValidator,
                                       AssertionValidator assertionValidator){

        super(assertionsSignatureValidator,assertionValidator);
    }

    @Override
    public TranslatedResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        validate(assertions, expectedInResponseTo, expectedLevelOfAssurance);
        return translateAssertions(assertions);
    }


    public void validate(List<Assertion> assertions, String requestId, LevelOfAssurance expectedLevelOfAssurance) {

        Map<AssertionType, List<Assertion>> assertionMap = assertions.stream()
                .collect(Collectors.groupingBy(this::classifyAssertion));

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

        validateIdPAssertion(authnAssertion, requestId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        validateIdPAssertion(mdsAssertion, requestId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);


        if (!mdsAssertion.getIssuer().getValue().equals(authnAssertion.getIssuer().getValue())) {
            throw new SamlResponseValidationException(MISMATCHED_ISSUERS);
        }

        if (!mdsAssertion.getSubject().getNameID().getValue().equals(authnAssertion.getSubject().getNameID().getValue())) {
            throw new SamlResponseValidationException(MISMATCHED_PIDS);
        }
    }
    private AssertionType classifyAssertion (Assertion assertion) {
        if (!assertion.getAuthnStatements().isEmpty()) {
            return AssertionType.AUTHN_ASSERTION;
        }
        else {
                return AssertionType.MDS_ASSERTION;
        }
    }
    private TranslatedResponseBody translateAssertions(List<Assertion> assertions) {
        return null;
    }

    @Override
    public TranslatedResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        return null;
    }
}
