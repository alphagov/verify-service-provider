package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.hub.factories.UserIdHashFactory;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.EidasAssertionTranslatorValidatorContainer;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;

import java.util.List;
import java.util.Optional;

public abstract class BaseEidasAssertionTranslator extends IdentityAssertionTranslator {

    private final static int ONLY_ONE_PRESENT = 0;

    private final InstantValidator instantValidator;
    private final ConditionsValidator conditionsValidator;
    private final LevelOfAssuranceValidator levelOfAssuranceValidator;
    private final List<String> acceptableHubConnectorEntityIds;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();

    final EidasMetadataResolverRepository metadataResolverRepository;
    final SignatureValidatorFactory signatureValidatorFactory;

    BaseEidasAssertionTranslator(
            EidasAssertionTranslatorValidatorContainer validatorContainer,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
            EidasMetadataResolverRepository metadataResolverRepository,
            SignatureValidatorFactory signatureValidatorFactory,
            List<String> acceptableHubConnectorEntityIds,
            UserIdHashFactory userIdHashFactory) {
        super(userIdHashFactory, validatorContainer.getSubjectValidator(), matchingDatasetUnmarshaller, mdsMapper);
        this.instantValidator = validatorContainer.getInstantValidator();
        this.conditionsValidator = validatorContainer.getConditionsValidator();
        this.levelOfAssuranceValidator = validatorContainer.getLevelOfAssuranceValidator();
        this.metadataResolverRepository = metadataResolverRepository;
        this.signatureValidatorFactory = signatureValidatorFactory;
        this.acceptableHubConnectorEntityIds = acceptableHubConnectorEntityIds;
    }

    protected abstract void validateSignature(Assertion assertion, String issuerEntityId);

    @Override
    public TranslatedNonMatchingResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        if (assertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one country assertion is expected.");
        }

        final Assertion countryAssertion = assertions.get(ONLY_ONE_PRESENT);
        validateCountryAssertion(countryAssertion, expectedInResponseTo);

        final LevelOfAssurance levelOfAssurance = extractLevelOfAssuranceFrom(countryAssertion);
        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);

        return translateAssertion(countryAssertion, levelOfAssurance, getAuthnContext(extractLevelOfAssuranceUriFrom(countryAssertion)));
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

    private Optional<AuthnContext> getAuthnContext(String uri) {
        AuthnContext authnContext = authnContextFactory.mapFromEidasToLoA(uri);
        return Optional.of(authnContext);
    }

    public Boolean isCountryAssertion(Assertion assertion) {
        return metadataResolverRepository.getResolverEntityIds().contains(assertion.getIssuer().getValue());
    }
}
