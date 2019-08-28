package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.IdentityAttributes;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedIdentityResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToIdentityAttributesMapper;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier.AssertionType;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_ISSUERS;
import static uk.gov.ida.saml.core.validation.errors.GenericHubProfileValidationSpecification.MISMATCHED_PIDS;
import static uk.gov.ida.verifyserviceprovider.dto.IdentityScenario.IDENTITY_VERIFIED;

public class VerifyAssertionTranslator extends IdentityAssertionTranslator {

    private final SamlAssertionsSignatureValidator assertionsSignatureValidator;
    private final AssertionAttributeStatementValidator attributeStatementValidator;
    private final AssertionClassifier assertionClassifierService;
    private final LevelOfAssuranceValidator levelOfAssuranceValidator;
    private UserIdHashFactory userIdHashFactory;

    public VerifyAssertionTranslator(
            SamlAssertionsSignatureValidator assertionsSignatureValidator,
            SubjectValidator subjectValidator,
            AssertionAttributeStatementValidator attributeStatementValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            AssertionClassifier assertionClassifierService,
            MatchingDatasetToIdentityAttributesMapper mdsMapper,
            LevelOfAssuranceValidator levelOfAssuranceValidator,
            UserIdHashFactory userIdHashFactory
    ) {
        super(subjectValidator, matchingDatasetUnmarshaller, mdsMapper);
        this.assertionsSignatureValidator = assertionsSignatureValidator;
        this.attributeStatementValidator = attributeStatementValidator;
        this.assertionClassifierService = assertionClassifierService;
        this.levelOfAssuranceValidator = levelOfAssuranceValidator;
        this.userIdHashFactory = userIdHashFactory;
    }


    @Override
    public TranslatedIdentityResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        Assertion authnAssertion = getAuthnAssertion(assertions);
        Assertion mdsAssertion = getMatchingDatasetAssertion(assertions);

        LevelOfAssurance levelOfAssurance = extractLevelOfAssuranceFrom(authnAssertion);

        validate(authnAssertion, mdsAssertion, expectedInResponseTo, expectedLevelOfAssurance, levelOfAssurance);

        String nameID = getNameIdFrom(mdsAssertion);
        String issuerID = mdsAssertion.getIssuer().getValue();
        String levelOfAssuranceUri =  extractLevelOfAssuranceUriFrom(authnAssertion);
        Optional<AuthnContext> authnContext = getAuthnContext(levelOfAssuranceUri);

        String hashId = userIdHashFactory.hashId(issuerID, nameID, authnContext);

        IdentityAttributes attributes = translateAttributes(mdsAssertion);

        return new TranslatedIdentityResponseBody(IDENTITY_VERIFIED, hashId, levelOfAssurance, attributes);
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

    private Optional<uk.gov.ida.saml.core.domain.AuthnContext> getAuthnContext(String uri) {
        return Arrays.stream(uk.gov.ida.saml.core.domain.AuthnContext.values())
                .filter(ctx -> uri.equals(ctx.getUri()))
                .findFirst();
    }

    public LevelOfAssurance extractLevelOfAssuranceFrom(Assertion authnAssertion) {
        String levelOfAssuranceUri = extractLevelOfAssuranceUriFrom(authnAssertion);

        try {
            return LevelOfAssurance.fromSamlValue(levelOfAssuranceUri);
        } catch (Exception ex) {
            throw new SamlResponseValidationException(String.format("Level of assurance '%s' is not supported.", levelOfAssuranceUri));
        }
    }
}
