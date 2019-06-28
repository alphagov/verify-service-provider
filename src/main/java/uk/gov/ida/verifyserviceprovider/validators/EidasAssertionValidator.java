package uk.gov.ida.verifyserviceprovider.validators;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;

import java.util.List;

import static java.util.Collections.singletonList;

public class EidasAssertionValidator extends IdentityAssertionValidator {
    private final InstantValidator instantValidator;
    private final ConditionsValidator conditionsValidator;
    private final SignatureValidatorFactory signatureValidatorFactory;
    private final String hubConnectorEntityId;
    private final MetadataResolverRepository metadataResolverRepository;
    private final SubjectValidator subjectValidator;


    public EidasAssertionValidator(
                                   InstantValidator instantValidator,
                                   ConditionsValidator conditionsValidator,
                                   SignatureValidatorFactory signatureValidatorFactory,
                                   String hubConnectorEntityId,
                                   MetadataResolverRepository metadataResolverRepository,
                                   SubjectValidator subjectValidator) {
        this.instantValidator = instantValidator;
        this.conditionsValidator = conditionsValidator;
        this.signatureValidatorFactory = signatureValidatorFactory;
        this.hubConnectorEntityId = hubConnectorEntityId;
        this.metadataResolverRepository = metadataResolverRepository;
        this.subjectValidator = subjectValidator;
    }

    public void validate(List<Assertion> assertions, String expectedInResponseTo) {

        if (assertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one country assertion is expected.");
        }

        Assertion assertion = assertions.get(0);

        validateSignature(assertion);

        instantValidator.validate(assertion.getIssueInstant(), "Country Assertion IssueInstant");

        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);

        conditionsValidator.validate(assertion.getConditions(), hubConnectorEntityId);

    }

    private void validateSignature(Assertion assertion) {
        String issuerEntityId = assertion.getIssuer().getValue();
        metadataResolverRepository.getSignatureTrustEngine(issuerEntityId)
            .map(signatureValidatorFactory::getSignatureValidator)
            .orElseThrow(() -> new SamlResponseValidationException("Unable to find metadata resolver for entity Id " + issuerEntityId))
            .validate(singletonList(assertion), IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }
}
