package uk.gov.ida.verifyserviceprovider;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.rules.NonMatchingVerifyServiceProviderAppRule;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.aValidEidasResponse;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.anInvalidAssertionSignatureEidasResponse;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.anInvalidSignatureEidasResponse;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;

public class NonMatchingEidasAcceptanceTest {

    @ClassRule
    public static NonMatchingVerifyServiceProviderAppRule application = new NonMatchingVerifyServiceProviderAppRule();

    @Test
    public void shouldProcessEidasResponseCorrectly() throws MarshallingException, SignatureException {
        String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
                aValidEidasResponse("requestId", application.getCountryEntityId()).build()
        );
        Response response = application.client().target(format("http://localhost:%s/translate-non-matching-response", application.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
        );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);

        JSONObject responseBody = new JSONObject(response.readEntity(String.class));
        assertThat(responseBody.getString("scenario")).isEqualTo(IDENTITY_VERIFIED.toString());
        assertThat(responseBody.getString("pid")).isNotBlank();
        assertThat(responseBody.getString("levelOfAssurance")).isEqualTo(LEVEL_2.toString());
    }

    @Test
    public void shouldReturn400WhenResponseContainsInvalidSignature() throws MarshallingException, SignatureException {
        String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
                anInvalidSignatureEidasResponse("requestId", application.getCountryEntityId()).build()
        );
        Response response = application.client().target(format("http://localhost:%s/translate-non-matching-response", application.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
        );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);

        String responseBody = response.readEntity(String.class);
        assertThat(responseBody).contains("Signature was not valid.");
    }

    @Test
    public void shouldReturn400WhenAssertionContainsInvalidSignature() throws MarshallingException, SignatureException {
        String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
                anInvalidAssertionSignatureEidasResponse("requestId", application.getCountryEntityId()).build()
        );
        Response response = application.client().target(format("http://localhost:%s/translate-non-matching-response", application.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
        );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);

        String responseBody = response.readEntity(String.class);
        assertThat(responseBody).contains("Signature was not valid.");
    }

    @Test
    public void shouldReturn400WhenAssertionSignedByCountryNotInTrustAnchor() throws MarshallingException, SignatureException {
        String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
                aValidEidasResponse("requestId", STUB_COUNTRY_ONE).build()
        );
        Response response = application.client().target(format("http://localhost:%s/translate-non-matching-response", application.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
        );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }
}
