package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public abstract class AssertionServiceV2 implements AssertionService<TranslatedNonMatchingResponseBody> {

    protected final SubjectValidator subjectValidator;
    private final MatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private final MatchingDatasetToNonMatchingAttributesMapper mdsMapper;


    public AssertionServiceV2(
            SubjectValidator subjectValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper
    ) {
        this.subjectValidator = subjectValidator;
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
        this.mdsMapper = mdsMapper;
    }


    @Override
    public TranslatedNonMatchingResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        Optional.ofNullable(statusCode.getStatusCode())
            .orElseThrow(() -> new SamlResponseValidationException("Missing status code for non-Success response"));
        String subStatus = statusCode.getStatusCode().getValue();

        switch (subStatus) {
            case StatusCode.REQUESTER:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.REQUEST_ERROR, null, null, null);
            case StatusCode.NO_AUTHN_CONTEXT:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.NO_AUTHENTICATION, null, null, null);
            case StatusCode.AUTHN_FAILED:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.AUTHENTICATION_FAILED, null, null, null);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML sub-status: %s", subStatus));
        }
    }

    protected NonMatchingAttributes translateAttributes(Assertion mdsAssertion) {
        MatchingDataset matchingDataset = matchingDatasetUnmarshaller.fromAssertion(mdsAssertion);

        return mdsMapper.mapToNonMatchingAttributes(matchingDataset);
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
