package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier.AssertionType;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static uk.gov.ida.saml.core.domain.AuthnContext.*;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_ISSUERS;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_PIDS;
import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;

public class NonMatchingAssertionService implements AssertionService<TranslatedNonMatchingResponseBody> {

    private final SamlAssertionsSignatureValidator assertionsSignatureValidator;
    private final SubjectValidator subjectValidator;
    private final AssertionAttributeStatementValidator attributeStatementValidator;
    private final MatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private final AssertionClassifier assertionClassifierService;
    private final MatchingDatasetToNonMatchingAttributesMapper mdsMapper;
    private final LevelOfAssuranceValidator levelOfAssuranceValidator;
    private UserIdHashFactory userIdHashFactory;

    public NonMatchingAssertionService(
            SamlAssertionsSignatureValidator assertionsSignatureValidator,
            SubjectValidator subjectValidator,
            AssertionAttributeStatementValidator attributeStatementValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            AssertionClassifier assertionClassifierService,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
            LevelOfAssuranceValidator levelOfAssuranceValidator,
            UserIdHashFactory userIdHashFactory) {
        this.assertionsSignatureValidator = assertionsSignatureValidator;
        this.subjectValidator = subjectValidator;
        this.attributeStatementValidator = attributeStatementValidator;
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
        this.assertionClassifierService = assertionClassifierService;
        this.mdsMapper = mdsMapper;
        this.levelOfAssuranceValidator = levelOfAssuranceValidator;
        this.userIdHashFactory = userIdHashFactory;
    }


    @Override
    public TranslatedNonMatchingResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        Assertion authnAssertion = getAuthnAssertion(assertions);
        Assertion mdsAssertion = getMatchingDatasetAssertion(assertions);

        LevelOfAssurance levelOfAssurance = extractLevelOfAssuranceFrom(authnAssertion);

        validate(authnAssertion, mdsAssertion, expectedInResponseTo, expectedLevelOfAssurance, levelOfAssurance);

        AuthnStatement authnStatement = authnAssertion.getAuthnStatements().get(0);
        String nameID = mdsAssertion.getSubject().getNameID().getValue();
        String issuerId =  authnAssertion.getIssuer().getValue();

        String hashId = userIdHashFactory.hashId(
                issuerId,
                nameID,
                Optional.of(
                        authnStatement.getAuthnContext().equals(LEVEL_1) ? LEVEL_1 :
                                authnStatement.getAuthnContext().equals(LEVEL_2) ? LEVEL_2 :
                                        authnStatement.getAuthnContext().equals(LEVEL_3) ? LEVEL_3 :
                                                authnStatement.getAuthnContext().equals(LEVEL_4) ? LEVEL_4 : LEVEL_X)
        );

        NonMatchingAttributes attributes = translateAttributes(mdsAssertion);

        return new TranslatedNonMatchingResponseBody(IDENTITY_VERIFIED, hashId, levelOfAssurance, attributes);
    }

    @Override
    public TranslatedNonMatchingResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        Optional.ofNullable(statusCode.getStatusCode())
            .orElseThrow(() -> new SamlResponseValidationException("Missing status code for non-Success response"));
        String subStatus = statusCode.getStatusCode().getValue();

        switch (subStatus) {
            case StatusCode.REQUESTER:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.REQUEST_ERROR, null, null, null);
            case StatusCode.NO_AUTHN_CONTEXT:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.CANCELLATION, null, null, null);
            case StatusCode.AUTHN_FAILED:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.AUTHENTICATION_FAILED, null, null, null);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML sub-status: %s", subStatus));
        }
    }


    public void validate(Assertion authnAssertion, Assertion mdsAssertion, String requestId, LevelOfAssurance expectedLevelOfAssurance, LevelOfAssurance levelOfAssurance) {

        validateIdpAssertion(authnAssertion, requestId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        validateIdpAssertion(mdsAssertion, requestId, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);

        if (!mdsAssertion.getIssuer().getValue().equals(authnAssertion.getIssuer().getValue())) {
            throw new SamlResponseValidationException(MISMATCHED_ISSUERS);
        }

        if (!mdsAssertion.getSubject().getNameID().getValue().equals(authnAssertion.getSubject().getNameID().getValue())) {
            throw new SamlResponseValidationException(MISMATCHED_PIDS);
        }
    }

    public void validateIdpAssertion(Assertion assertion,
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
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        attributeStatementValidator.validate(assertion);
    }


    public NonMatchingAttributes translateAttributes(Assertion mdsAssertion) {
        MatchingDataset matchingDataset = matchingDatasetUnmarshaller.fromAssertion(mdsAssertion);

        return mdsMapper.mapToNonMatchingAttributes(matchingDataset);
    }


    private Assertion getAuthnAssertion(Collection<Assertion> assertions) {
        Map<AssertionType, List<Assertion>> assertionMap = assertions.stream()
                .collect(Collectors.groupingBy(assertionClassifierService::classifyAssertion));

        List<Assertion> authnAssertions = assertionMap.get(AssertionType.AUTHN_ASSERTION);
        if (authnAssertions == null || authnAssertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one authn statement is expected.");
        }

        return authnAssertions.get(0);
    }

    private Assertion getMatchingDatasetAssertion(Collection<Assertion> assertions) {
        Map<AssertionType, List<Assertion>> assertionMap = assertions.stream()
                .collect(Collectors.groupingBy(assertionClassifierService::classifyAssertion));

        List<Assertion> mdsAssertions = assertionMap.get(AssertionType.MDS_ASSERTION);
        if (mdsAssertions == null || mdsAssertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one matching dataset assertion is expected.");
        }

        return mdsAssertions.get(0);
    }

    public LevelOfAssurance extractLevelOfAssuranceFrom(Assertion authnAssertion) {
        String levelOfAssuranceString = extractLevelOfAssuranceStringFrom(authnAssertion);

        try {
            return LevelOfAssurance.fromSamlValue(levelOfAssuranceString);
        } catch (Exception ex) {
            throw new SamlResponseValidationException(String.format("Level of assurance '%s' is not supported.", levelOfAssuranceString));
        }
    }

    private String extractLevelOfAssuranceStringFrom(Assertion authnAssertion) {
        AuthnStatement authnStatement = authnAssertion.getAuthnStatements().get(0);
        return ofNullable(authnStatement.getAuthnContext())
                .map(AuthnContext::getAuthnContextClassRef)
                .map(AuthnContextClassRef::getAuthnContextClassRef)
                .orElseThrow(() -> new SamlResponseValidationException("Expected a level of assurance."));
    }
}
