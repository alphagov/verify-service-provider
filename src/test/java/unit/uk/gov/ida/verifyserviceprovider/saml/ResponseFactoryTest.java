package unit.uk.gov.ida.verifyserviceprovider.saml;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;

import java.util.Base64;
import java.util.Collections;


public class ResponseFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private StringToOpenSamlObjectTransformer<Response> stringToResponseTransformer = ResponseFactory.createStringToResponseTransformer();

    @Test
    public void createStringToResponseTransformerShouldNotAllowNullSamlResponse() {
        expectedException.expect(SamlTransformationErrorException.class);
        expectedException.expectMessage("SAML Validation Specification: Missing SAML message.");
        stringToResponseTransformer.apply(null);
    }

    @Test
    public void createStringToResponseTransformerMustContainBase64EncodedSamlResponse() {
        expectedException.expect(SamlTransformationErrorException.class);
        expectedException.expectMessage("SAML Validation Specification: SAML is not base64 encoded in message body. start> not-encoded-string <end");
        stringToResponseTransformer.apply("not-encoded-string");
    }

    @Test
    public void createStringToResponseTransformerShouldNotAllowTooLongSamlMessages() {
        String longString = String.join("", Collections.nCopies(50001, "a"));
        String longBase64EncodedString = Base64.getEncoder().encodeToString(longString.getBytes());
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("SAML Response is too long.");
        stringToResponseTransformer.apply(longBase64EncodedString);
    }
}