package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier.AssertionType;
import uk.gov.ida.verifyserviceprovider.validators.IdentityAssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.VerifyAssertionsValidator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VerifyAssertionTranslator extends IdentityAssertionTranslator {

    private final AssertionClassifier assertionClassifierService;
    public final IdentityAssertionValidator verifyAssertionValidator;

    public VerifyAssertionTranslator(
        MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
        AssertionClassifier assertionClassifierService,
        MatchingDatasetToNonMatchingAttributesMapper mdsMapper,
        UserIdHashFactory userIdHashFactory,
        VerifyAssertionsValidator assertionValidator,
        LevelOfAssuranceValidator levelOfAssuranceValidator) {
        super(matchingDatasetUnmarshaller, mdsMapper, userIdHashFactory, assertionValidator, levelOfAssuranceValidator);
        this.assertionClassifierService = assertionClassifierService;
        this.verifyAssertionValidator = assertionValidator;
    }

    @Override
    protected Assertion authnContextAssertion(List<Assertion> assertions) {
        return getAuthnAssertion(assertions);
    }

    @Override
    protected Assertion attributeAssertion(List<Assertion> assertions) {
        return getMatchingDatasetAssertion(assertions);
    }

    @Override
    protected LevelOfAssurance translateUriToLOA(String levelOfAssuranceString) {
        return LevelOfAssurance.fromSamlValue(levelOfAssuranceString);
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

    @Override
    protected Optional<uk.gov.ida.saml.core.domain.AuthnContext> getAuthnContext(String uri) {
        return Arrays.stream(uk.gov.ida.saml.core.domain.AuthnContext.values())
                .filter(ctx -> uri.equals(ctx.getUri()))
                .findFirst();
    }
}
