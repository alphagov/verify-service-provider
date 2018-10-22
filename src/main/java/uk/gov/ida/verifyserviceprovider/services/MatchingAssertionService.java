package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.core.domain.SamlStatusCode;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.Scenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.dto.Scenario.ACCOUNT_CREATION;
import static uk.gov.ida.verifyserviceprovider.dto.Scenario.SUCCESS_MATCH;

public class MatchingAssertionService extends AssertionService                                                                                                                                                                                                  {


    public MatchingAssertionService(
        SamlAssertionsSignatureValidator assertionsSignatureValidator,
        AssertionValidator assertionValidator
    ) {
        super(assertionsSignatureValidator,assertionValidator);

    }

    public TranslatedResponseBody translateSuccessResponse(
            List<Assertion> assertions,
            String expectedInResponseTo,
            LevelOfAssurance expectedLevelOfAssurance,
            String entityId
    ) {
        //  1. check saml has assertions
        checkSamlhasAssertions(assertions);
        //  2. validate assertions
        Assertion assertion = assertions.get(0);
        assertionValidator.validate(assertion, expectedInResponseTo, entityId);
        assertionsSignatureValidator.validate(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        //  3. validate levelOfAssurance
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);
        LevelOfAssurance levelOfAssurance = extractLevelOfAssurance(authnStatement);
        LevelOfAssuranceValidator levelOfAssuranceValidator = new LevelOfAssuranceValidator();
        levelOfAssuranceValidator.validate(levelOfAssurance, expectedLevelOfAssurance);
        //  4. translateAssertions
        String nameID = assertion.getSubject().getNameID().getValue();
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        if (isUserAccountCreation(attributeStatements)) {
            return new TranslatedResponseBody(
                ACCOUNT_CREATION,
                nameID,
                levelOfAssurance,
                AttributeTranslationService.translateAttributes(attributeStatements.get(0))
            );

        }
        return new TranslatedResponseBody(SUCCESS_MATCH, nameID, levelOfAssurance, null);

    }

    public TranslatedResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        Optional.ofNullable(statusCode.getStatusCode())
                .orElseThrow(() -> new SamlResponseValidationException("Missing status code for non-Success response"));
        String subStatus = statusCode.getStatusCode().getValue();

        switch (subStatus) {
            case SamlStatusCode.NO_MATCH:
                return new TranslatedResponseBody(Scenario.NO_MATCH, null, null, null);
            case StatusCode.REQUESTER:
                return new TranslatedResponseBody(Scenario.REQUEST_ERROR, null, null, null);
            case StatusCode.NO_AUTHN_CONTEXT:
                return new TranslatedResponseBody(Scenario.CANCELLATION, null, null, null);
            case StatusCode.AUTHN_FAILED:
                return new TranslatedResponseBody(Scenario.AUTHENTICATION_FAILED, null, null, null);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML sub-status: %s", subStatus));
        }
    }

    private boolean isUserAccountCreation(List<AttributeStatement> attributeStatements) {
        return !attributeStatements.isEmpty();
    }

    private void checkSamlhasAssertions(List<Assertion> assertions) {
        if (assertions == null || assertions.size() != 1) {
            throw new SamlResponseValidationException("Exactly one assertion is expected.");
        }
    }

    private LevelOfAssurance extractLevelOfAssurance(AuthnStatement authnStatement) {
        String levelOfAssuranceString = ofNullable(authnStatement.getAuthnContext())
            .map(AuthnContext::getAuthnContextClassRef)
            .map(AuthnContextClassRef::getAuthnContextClassRef)
            .orElseThrow(() -> new SamlResponseValidationException("Expected a level of assurance."));

        try {
            return LevelOfAssurance.fromSamlValue(levelOfAssuranceString);
        } catch (Exception ex) {
            throw new SamlResponseValidationException(String.format("Level of assurance '%s' is not supported.", levelOfAssuranceString));
        }
    }
}
