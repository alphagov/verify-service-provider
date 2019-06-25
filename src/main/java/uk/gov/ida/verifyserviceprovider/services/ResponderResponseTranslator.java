package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

public interface ResponderResponseTranslator {
    TranslatedResponseBody translateResponderResponse(StatusCode statusCode);
}
