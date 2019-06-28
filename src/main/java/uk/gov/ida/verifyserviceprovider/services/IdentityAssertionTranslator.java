package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.validators.IdentityAssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;

public abstract class IdentityAssertionTranslator implements AssertionTranslator {

    private final MatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private final MatchingDatasetToNonMatchingAttributesMapper mdsMapper;
    private final UserIdHashFactory userIdHashFactory;
    private final IdentityAssertionValidator assertionValidator;
    private LevelOfAssuranceValidator levelOfAssuranceValidator;

    IdentityAssertionTranslator(
        MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
        MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
        UserIdHashFactory userIdHashFactory,
        IdentityAssertionValidator assertionValidator,
        LevelOfAssuranceValidator levelOfAssuranceValidator) {
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
        this.mdsMapper = mdsMapper;
        this.userIdHashFactory = userIdHashFactory;
        this.assertionValidator = assertionValidator;
        this.levelOfAssuranceValidator = levelOfAssuranceValidator;
    }

    @Override
    public TranslatedNonMatchingResponseBody translateSuccessResponse(List<Assertion> assertions,
                                                                            String expectedInResponseTo,
                                                                            LevelOfAssurance expectedLevelOfAssurance,
                                                                            String entityId) {

        Assertion authnContextAssertion = authnContextAssertion(assertions);
        Assertion attributeAssertion = attributeAssertion(assertions);

        LevelOfAssurance levelOfAssurance = extractLevelOfAssuranceFrom(authnContextAssertion);


        validate(assertions, expectedInResponseTo, expectedLevelOfAssurance, levelOfAssurance);

        String hashId = createHashedPid(authnContextAssertion);

        NonMatchingAttributes attributes = translateAttributes(attributeAssertion);

        return new TranslatedNonMatchingResponseBody(IDENTITY_VERIFIED, hashId, levelOfAssurance, attributes);
    }

    public LevelOfAssurance extractLevelOfAssuranceFrom(Assertion countryAssertion) {
        String levelOfAssuranceString = extractLevelOfAssuranceUriFrom(countryAssertion);

        try {
            return translateUriToLOA(levelOfAssuranceString);
        } catch (Exception ex) {
            throw new SamlResponseValidationException(String.format("Level of assurance '%s' is not supported.", levelOfAssuranceString));
        }
    }

    private NonMatchingAttributes translateAttributes(Assertion mdsAssertion) {
        MatchingDataset matchingDataset = matchingDatasetUnmarshaller.fromAssertion(mdsAssertion);

        return mdsMapper.mapToNonMatchingAttributes(matchingDataset);
    }

    private String extractLevelOfAssuranceUriFrom(Assertion assertion) {
        AuthnStatement authnStatement = getAuthnStatementFrom(assertion);
        return ofNullable(authnStatement.getAuthnContext())
            .map(AuthnContext::getAuthnContextClassRef)
            .map(AuthnContextClassRef::getAuthnContextClassRef)
            .orElseThrow(() -> new SamlResponseValidationException("Expected a level of assurance."));
    }

    private String createHashedPid(Assertion assertion) {
        String nameID = getNameIdFrom(assertion);
        String issuerID = assertion.getIssuer().getValue();
        String levelOfAssuranceUri =  extractLevelOfAssuranceUriFrom(assertion);
        Optional<uk.gov.ida.saml.core.domain.AuthnContext> authnContext = getAuthnContext(levelOfAssuranceUri);

        return userIdHashFactory.hashId(issuerID, nameID, authnContext);
    }

    private AuthnStatement getAuthnStatementFrom(Assertion assertion) {
        return assertion.getAuthnStatements().get(0);
    }

    private String getNameIdFrom(Assertion assertion) {
        return assertion.getSubject().getNameID().getValue();
    }

    protected void validate(List<Assertion> assertions, String requestId, LevelOfAssurance expectedLevelOfAssurance, LevelOfAssurance levelOfAssurance) {
        assertionValidator.validate(assertions, requestId);
        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);
    }

    protected abstract Assertion authnContextAssertion(List<Assertion> assertion);

    protected abstract Assertion attributeAssertion(List<Assertion> assertion);

    protected abstract LevelOfAssurance translateUriToLOA(String levelOfAssuranceString);

    protected abstract Optional<uk.gov.ida.saml.core.domain.AuthnContext> getAuthnContext(String uri);
}
