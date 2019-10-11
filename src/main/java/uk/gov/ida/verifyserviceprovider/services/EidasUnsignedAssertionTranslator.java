package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;

public class EidasUnsignedAssertionTranslator extends BaseEidasAssertionTranslator {
    public EidasUnsignedAssertionTranslator(
            SubjectValidator subjectValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
            InstantValidator instantValidator,
            ConditionsValidator conditionsValidator,
            LevelOfAssuranceValidator levelOfAssuranceValidator,
            EidasMetadataResolverRepository metadataResolverRepository,
            List<String> acceptableHubConnectorEntityIds,
            UserIdHashFactory userIdHashFactory) {
            super(
                subjectValidator,
                matchingDatasetUnmarshaller,
                mdsMapper,
                instantValidator,
                conditionsValidator,
                levelOfAssuranceValidator,
                metadataResolverRepository,
                null,
                acceptableHubConnectorEntityIds,
                userIdHashFactory
        );
    }

    @Override
    protected void validateSignature(Assertion assertion, String issuerEntityId) {
        // No need to validate the signature of an unsigned assertion.
    }
}
