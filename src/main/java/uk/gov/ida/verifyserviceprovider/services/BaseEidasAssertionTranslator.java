package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;
import java.util.Optional;

import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;

public abstract class BaseEidasAssertionTranslator extends IdentityAssertionTranslator {

    private final InstantValidator instantValidator;
    private final ConditionsValidator conditionsValidator;
    private final LevelOfAssuranceValidator levelOfAssuranceValidator;
    final EidasMetadataResolverRepository metadataResolverRepository;
    final SignatureValidatorFactory signatureValidatorFactory;
    private final List<String> acceptableHubConnectorEntityIds;
    private UserIdHashFactory userIdHashFactory;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();

    public BaseEidasAssertionTranslator(
            SubjectValidator subjectValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
            InstantValidator instantValidator,
            ConditionsValidator conditionsValidator,
            LevelOfAssuranceValidator levelOfAssuranceValidator,
            EidasMetadataResolverRepository metadataResolverRepository,
            SignatureValidatorFactory signatureValidatorFactory,
            List<String> acceptableHubConnectorEntityIds,
            UserIdHashFactory userIdHashFactory) {
        super(subjectValidator, matchingDatasetUnmarshaller, mdsMapper);
        this.instantValidator = instantValidator;
        this.conditionsValidator = conditionsValidator;
        this.levelOfAssuranceValidator = levelOfAssuranceValidator;
        this.metadataResolverRepository = metadataResolverRepository;
        this.signatureValidatorFactory = signatureValidatorFactory;
        this.acceptableHubConnectorEntityIds = acceptableHubConnectorEntityIds;
        this.userIdHashFactory = userIdHashFactory;
    }

    abstract void validateSignature(Assertion assertion, String issuerEntityId);

    @Override
    public TranslatedNonMatchingResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        if (assertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one country assertion is expected.");
        }

        Assertion countryAssertion = assertions.get(0);

        validateCountryAssertion(countryAssertion, expectedInResponseTo);

        LevelOfAssurance levelOfAssurance = extractLevelOfAssuranceFrom(countryAssertion);
        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);

        String nameID = getNameIdFrom(countryAssertion);
        String issuerID = countryAssertion.getIssuer().getValue();
        String levelOfAssuranceUri =  extractLevelOfAssuranceUriFrom(countryAssertion);
        Optional<AuthnContext> authnContext = getAuthnContext(levelOfAssuranceUri);
        String hashId = userIdHashFactory.hashId(issuerID, nameID, authnContext);

        NonMatchingAttributes attributes = translateAttributes(countryAssertion);

        return new TranslatedNonMatchingResponseBody(IDENTITY_VERIFIED, hashId, levelOfAssurance, attributes);
    }

    private void validateCountryAssertion(Assertion assertion, String expectedInResponseTo) {
        String issuerEntityId = assertion.getIssuer().getValue();
        validateSignature(assertion, issuerEntityId);
        instantValidator.validate(assertion.getIssueInstant(), "Country Assertion IssueInstant");
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        conditionsValidator.validate(assertion.getConditions(), acceptableHubConnectorEntityIds.toArray(new String[0]));
    }

    public LevelOfAssurance extractLevelOfAssuranceFrom(Assertion countryAssertion) {
        String levelOfAssuranceString = extractLevelOfAssuranceUriFrom(countryAssertion);

        try {
            return LevelOfAssurance.fromSamlValue(new AuthnContextFactory().mapFromEidasToLoA(levelOfAssuranceString).getUri());
        } catch (Exception ex) {
            throw new SamlResponseValidationException(String.format("Level of assurance '%s' is not supported.", levelOfAssuranceString));
        }
    }

    Optional<AuthnContext> getAuthnContext(String uri) {
        AuthnContext authnContext = authnContextFactory.mapFromEidasToLoA(uri);

        return Optional.of(authnContext);
    }

    public Boolean isCountryAssertion(Assertion assertion) {
        return metadataResolverRepository.getResolverEntityIds().contains(assertion.getIssuer().getValue());
    }

}
