package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.verifyserviceprovider.dto.IdentityAttributes;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToIdentityAttributesMapper;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;

import static java.util.Optional.ofNullable;

public abstract class IdentityAssertionTranslator implements AssertionTranslator {

    final SubjectValidator subjectValidator;
    private final MatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private final MatchingDatasetToIdentityAttributesMapper mdsMapper;


    public IdentityAssertionTranslator(
            SubjectValidator subjectValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToIdentityAttributesMapper mdsMapper
    ) {
        this.subjectValidator = subjectValidator;
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
        this.mdsMapper = mdsMapper;
    }

    abstract public TranslatedResponseBody translateSuccessResponse(
        List<Assertion> assertions,
        String expectedInResponseTo,
        LevelOfAssurance expectedLevelOfAssurance,
        String entityId
    );


    protected IdentityAttributes translateAttributes(Assertion mdsAssertion) {
        MatchingDataset matchingDataset = matchingDatasetUnmarshaller.fromAssertion(mdsAssertion);

        return mdsMapper.mapToIdentityAttributes(matchingDataset);
    }

    protected String getNameIdFrom(Assertion assertion) {
        return assertion.getSubject().getNameID().getValue();
    }

    protected AuthnStatement getAuthnStatementFrom(Assertion assertion) {
        return assertion.getAuthnStatements().get(0);
    }

    protected String extractLevelOfAssuranceUriFrom(Assertion assertion) {
        AuthnStatement authnStatement = getAuthnStatementFrom(assertion);
        return ofNullable(authnStatement.getAuthnContext())
            .map(AuthnContext::getAuthnContextClassRef)
            .map(AuthnContextClassRef::getAuthnContextClassRef)
            .orElseThrow(() -> new SamlResponseValidationException("Expected a level of assurance."));
    }
}
