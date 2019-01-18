package uk.gov.ida.verifyserviceprovider;

import org.apache.http.HttpStatus;
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
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class NonMatchingEidasAcceptanceTest {

    @ClassRule
    public static NonMatchingVerifyServiceProviderAppRule appWithoutEidasConfig = new NonMatchingVerifyServiceProviderAppRule();
    @ClassRule
    public static NonMatchingVerifyServiceProviderAppRule appWithEidasEnabled = new NonMatchingVerifyServiceProviderAppRule(true);
    @ClassRule
    public static NonMatchingVerifyServiceProviderAppRule appWithEidasDisabled = new NonMatchingVerifyServiceProviderAppRule(false);

     @Test
     public void shouldReturn400WhenAssertionContainsInvalidSignature() throws MarshallingException, SignatureException {
         String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
                 anInvalidAssertionSignatureEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId()).build()
         );
         Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-non-matching-response", appWithEidasEnabled.getLocalPort())).request().post(
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
         Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-non-matching-response", appWithEidasEnabled.getLocalPort())).request().post(
                 Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldReturn400ForEidasResponseWhenEuropeanIdentityConfigAbsent() throws Exception {
         String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
             aValidEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId()).build()
         );
         Response response = appWithoutEidasConfig.client().target(format("http://localhost:%s/translate-non-matching-response", appWithoutEidasConfig.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldReturn400ForEidasResponseWhenEuropeanIdentityDisabled() throws Exception {
         String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
             aValidEidasResponse("requestId", appWithEidasDisabled.getCountryEntityId()).build()
         );
         Response response = appWithEidasDisabled.client().target(format("http://localhost:%s/translate-non-matching-response", appWithEidasDisabled.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldProcessEidasResponseCorrectlyWhenEuropeanIdentityEnabled() throws Exception {
         String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
             aValidEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId()).build()
         );
         Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-non-matching-response", appWithEidasEnabled.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
     }

}
