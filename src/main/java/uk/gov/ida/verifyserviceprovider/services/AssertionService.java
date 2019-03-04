package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;

import java.util.List;

public interface AssertionService<T> {

    T translateSuccessResponse(
            List<Assertion> assertions,
            String expectedInResponseTo,
            LevelOfAssurance expectedLevelOfAssurance,
            String entityId
    );

    T translateNonSuccessResponse( StatusCode statusCode);

}
