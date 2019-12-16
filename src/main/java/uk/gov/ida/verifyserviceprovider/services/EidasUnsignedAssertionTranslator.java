package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.hub.factories.UserIdHashFactory;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.verifyserviceprovider.validators.EidasAssertionTranslatorValidatorContainer;

import java.util.List;

public class EidasUnsignedAssertionTranslator extends BaseEidasAssertionTranslator {
    public EidasUnsignedAssertionTranslator(
            EidasAssertionTranslatorValidatorContainer validatorContainer,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
            EidasMetadataResolverRepository metadataResolverRepository,
            List<String> acceptableHubConnectorEntityIds,
            UserIdHashFactory userIdHashFactory) {
        super(
                validatorContainer,
                matchingDatasetUnmarshaller,
                mdsMapper,
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
