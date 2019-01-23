package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.List;
import java.util.Optional;

public class ClassifyingAssertionService implements AssertionService<TranslatedNonMatchingResponseBody> {

    private final IdpAssertionService idpAssertionService;
    private final EidasAssertionService eidasAssertionService;


    public ClassifyingAssertionService(
            IdpAssertionService idpAssertionService,
            EidasAssertionService eidasAssertionService
    ) {
        this.idpAssertionService = idpAssertionService;
        this.eidasAssertionService = eidasAssertionService;
    }


    @Override
    public TranslatedNonMatchingResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        AssertionServiceV2 assertionService = isCountryAttributeQuery(assertions) ? eidasAssertionService : idpAssertionService;

        return assertionService.translateSuccessResponse(assertions, expectedInResponseTo, expectedLevelOfAssurance, entityId);
    }

    //This is a duplicate of AssertionServiceV2's implementations
    @Override
    public TranslatedNonMatchingResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        Optional.ofNullable(statusCode.getStatusCode())
            .orElseThrow(() -> new SamlResponseValidationException("Missing status code for non-Success response"));
        String subStatus = statusCode.getStatusCode().getValue();

        switch (subStatus) {
            case StatusCode.REQUESTER:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.REQUEST_ERROR, null, null, null);
            case StatusCode.NO_AUTHN_CONTEXT:
                //FIXME CANCELLATION is not correct
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.CANCELLATION, null, null, null);
            case StatusCode.AUTHN_FAILED:
                return new TranslatedNonMatchingResponseBody(NonMatchingScenario.AUTHENTICATION_FAILED, null, null, null);
            default:
                throw new SamlResponseValidationException(String.format("Unknown SAML sub-status: %s", subStatus));
        }
    }


    //FIXME should be something like isCountryResponse
    private boolean isCountryAttributeQuery(List<Assertion> assertions) {
        return assertions.stream().anyMatch(eidasAssertionService::isCountryAssertion);
    }

}
