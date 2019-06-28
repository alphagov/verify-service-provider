package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.validators.EidasAssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;

import java.util.List;
import java.util.Optional;

public class EidasAssertionTranslator extends IdentityAssertionTranslator {

    private final EidasMetadataResolverRepository metadataResolverRepository;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();

    public EidasAssertionTranslator(
        MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
        MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
        EidasMetadataResolverRepository metadataResolverRepository,
        UserIdHashFactory userIdHashFactory,
        EidasAssertionValidator eidasAssertionValidator,
        LevelOfAssuranceValidator levelOfAssuranceValidator) {
        super(
            matchingDatasetUnmarshaller,
            mdsMapper,
            userIdHashFactory,
            eidasAssertionValidator,
            levelOfAssuranceValidator
        );
        this.metadataResolverRepository = metadataResolverRepository;
    }


    @Override
    public Assertion authnContextAssertion(List<Assertion> assertion) {
        return eidasAssertion(assertion);
    }

    @Override
    public Assertion attributeAssertion(List<Assertion> assertion) {
       return eidasAssertion(assertion);
    }

    private Assertion eidasAssertion(List<Assertion> assertion) {
        return assertion.get(0);
    }

    @Override
    protected LevelOfAssurance translateUriToLOA(String levelOfAssuranceString) {
        return LevelOfAssurance.fromSamlValue(authnContextFactory.mapFromEidasToLoA(levelOfAssuranceString).getUri());
    }

    @Override
    protected Optional<AuthnContext> getAuthnContext(String uri) {
        AuthnContext authnContext = authnContextFactory.mapFromEidasToLoA(uri);

        return Optional.of(authnContext);
    }

    public Boolean isCountryAssertion(Assertion assertion) {
        return metadataResolverRepository.getResolverEntityIds().contains(assertion.getIssuer().getValue());
    }
}
