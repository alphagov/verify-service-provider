package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.verifyserviceprovider.dto.AttributesV2;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToAttributesMapper;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;

public class EidasAssertionService extends AssertionServiceV2 {

    private final InstantValidator instantValidator;
    private final ConditionsValidator conditionsValidator;
    private final LevelOfAssuranceValidator levelOfAssuranceValidator;
    private final MetadataResolverRepository metadataResolverRepository;
    private final SignatureValidatorFactory signatureValidatorFactory;


    public EidasAssertionService(
            SubjectValidator subjectValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToAttributesMapper mdsMapper,
            InstantValidator instantValidator,
            ConditionsValidator conditionsValidator,
            LevelOfAssuranceValidator levelOfAssuranceValidator,
            MetadataResolverRepository metadataResolverRepository,
            SignatureValidatorFactory signatureValidatorFactory) {
        super(subjectValidator, matchingDatasetUnmarshaller, mdsMapper);
        this.instantValidator = instantValidator;
        this.conditionsValidator = conditionsValidator;
        this.levelOfAssuranceValidator = levelOfAssuranceValidator;
        this.metadataResolverRepository = metadataResolverRepository;
        this.signatureValidatorFactory = signatureValidatorFactory;
    }


    @Override
    public TranslatedNonMatchingResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        if (assertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one country assertion is expected.");
        }

        Assertion countryAssertion = assertions.get(0);

        validateCountryAssertion(countryAssertion, expectedInResponseTo, entityId);

        LevelOfAssurance levelOfAssurance = extractLevelOfAssuranceFrom(countryAssertion);
        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);

        String nameID = getNameIdFrom(countryAssertion);

        AttributesV2 attributes = translateAttributes(countryAssertion);

        return new TranslatedNonMatchingResponseBody(IDENTITY_VERIFIED, nameID, levelOfAssurance, attributes);
    }

    private void validateCountryAssertion(Assertion assertion, String expectedInResponseTo, String entityId) {
        signatureValidatorFactory.getSignatureValidator(metadataResolverRepository.getSignatureTrustEngine(assertion.getIssuer().getValue()))
                .orElseThrow(() -> new SamlResponseValidationException("Unable to find metadata resolver for entity Id " + assertion.getIssuer().getValue()))
                .validate(singletonList(assertion), IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        instantValidator.validate(assertion.getIssueInstant(), "Country Assertion IssueInstant");
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        conditionsValidator.validate(assertion.getConditions(), entityId);
    }

    public LevelOfAssurance extractLevelOfAssuranceFrom(Assertion countryAssertion) {
        String levelOfAssuranceString = extractLevelOfAssuranceUriFrom(countryAssertion);

        try {
            return LevelOfAssurance.fromSamlValue(new AuthnContextFactory().mapFromEidasToLoA(levelOfAssuranceString).getUri());
        } catch (Exception ex) {
            throw new SamlResponseValidationException(String.format("Level of assurance '%s' is not supported.", levelOfAssuranceString));
        }
    }

    public Boolean isCountryAssertion(Assertion assertion) {
        return metadataResolverRepository.getResolverEntityIds().contains(assertion.getIssuer().getValue());
    }

}
