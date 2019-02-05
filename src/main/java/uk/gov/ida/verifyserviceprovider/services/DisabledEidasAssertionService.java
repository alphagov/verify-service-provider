package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;

import java.util.List;

public class DisabledEidasAssertionService extends EidasAssertionService {
    class EidasSupportDisabledException extends RuntimeException {

        public EidasSupportDisabledException(String message) {
            super(message);
        }
    }
    public DisabledEidasAssertionService() {
        super(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
                );
    }

    @Override
    public TranslatedNonMatchingResponseBody translateSuccessResponse(List<Assertion> assertions, String expectedInResponseTo, LevelOfAssurance expectedLevelOfAssurance, String entityId) {
        throw new EidasSupportDisabledException("eIDAS is disabled");
    }

    @Override
    public LevelOfAssurance extractLevelOfAssuranceFrom(Assertion countryAssertion) {
        throw new EidasSupportDisabledException("eIDAS is disabled");
    }

    @Override
    public Boolean isCountryAssertion(Assertion assertion) {
        return Boolean.FALSE;
    }

    @Override
    public TranslatedNonMatchingResponseBody translateNonSuccessResponse(StatusCode statusCode) {
        throw new EidasSupportDisabledException("eIDAS is disabled");
    }
}
