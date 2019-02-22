package uk.gov.ida.verifyserviceprovider;

import org.apache.http.HttpStatus;
import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.extensions.eidas.CurrentFamilyName;
import uk.gov.ida.saml.core.extensions.eidas.CurrentGivenName;
import uk.gov.ida.saml.core.extensions.eidas.DateOfBirth;
import uk.gov.ida.saml.core.extensions.eidas.PersonIdentifier;
import uk.gov.ida.saml.core.extensions.eidas.impl.CurrentFamilyNameBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.CurrentGivenNameBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.DateOfBirthBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.PersonIdentifierBuilder;
import uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder;
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

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    @Before
    public void setUp() throws Exception {
        openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    }

    @Before


    @Test
     public void shouldReturn400WhenAssertionContainsInvalidSignature() throws MarshallingException, SignatureException {
         String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
                 anInvalidAssertionSignatureEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId()).build()
         );
         Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
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
         Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
                 Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldReturn400ForEidasResponseWhenEuropeanIdentityConfigAbsent() throws Exception {
         String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
             aValidEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId()).build()
         );
         Response response = appWithoutEidasConfig.client().target(format("http://localhost:%s/translate-response", appWithoutEidasConfig.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldReturn400ForEidasResponseWhenEuropeanIdentityDisabled() throws Exception {
         String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
             aValidEidasResponse("requestId", appWithEidasDisabled.getCountryEntityId()).build()
         );
         Response response = appWithEidasDisabled.client().target(format("http://localhost:%s/translate-response", appWithEidasDisabled.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldProcessEidasResponseCorrectlyWhenEuropeanIdentityEnabled() throws Exception {
         String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
             aValidEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId()).build()
         );
         Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
     }

    @Test
    public void shouldMapAttributesCorrectly() throws Exception {
        AttributeStatementBuilder attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement();
        
        Attribute firstName =  anAttribute(IdaConstants.Eidas_Attributes.FirstName.NAME);
        CurrentGivenName firstNameValue = new CurrentGivenNameBuilder().buildObject();
        firstNameValue.setFirstName("Joe");
        firstName.getAttributeValues().add(firstNameValue);
        CurrentGivenName nonLatinScriptFirstNameValue = new CurrentGivenNameBuilder().buildObject();
        nonLatinScriptFirstNameValue.setFirstName("NonLatinJoe");
        nonLatinScriptFirstNameValue.setIsLatinScript(false);
        firstName.getAttributeValues().add(nonLatinScriptFirstNameValue);

        attributeStatementBuilder.addAttribute(firstName);

        Attribute familyName =  anAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME);

        CurrentFamilyName familyNameValue = new CurrentFamilyNameBuilder().buildObject();
        familyNameValue.setFamilyName("Bloggs");
        familyName.getAttributeValues().add(familyNameValue);

        CurrentFamilyName nonLatinFamilyNameValue = new CurrentFamilyNameBuilder().buildObject();
        nonLatinFamilyNameValue.setFamilyName("NonLatinBloggs");
        nonLatinFamilyNameValue.setIsLatinScript(false);
        familyName.getAttributeValues().add(nonLatinFamilyNameValue);

        attributeStatementBuilder.addAttribute(familyName);

        Attribute personIdentifier =  anAttribute(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME);
        PersonIdentifier personIdentifierValue = new PersonIdentifierBuilder().buildObject();
        personIdentifierValue.setPersonIdentifier("JB12345");
        personIdentifier.getAttributeValues().add(personIdentifierValue);
        attributeStatementBuilder.addAttribute(personIdentifier);

        Attribute dateOfBirth =  anAttribute(IdaConstants.Eidas_Attributes.DateOfBirth.NAME);
        DateOfBirth dateOfBirthValue = new DateOfBirthBuilder().buildObject();
        dateOfBirthValue.setDateOfBirth(LocalDate.now());
        dateOfBirth.getAttributeValues().add(dateOfBirthValue);
        attributeStatementBuilder.addAttribute(dateOfBirth);

        org.opensaml.saml.saml2.core.Response samlResponse = aValidEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId(),
                attributeStatementBuilder.build()
                ).build();
        String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(samlResponse);
        Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
        );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        String body = response.readEntity(String.class);
        JSONObject json = new JSONObject(body);
        JSONObject attributes = json.getJSONObject("attributes");
        JSONObject firstNames = attributes.getJSONArray("firstNames").getJSONObject(0);

        assertThat(firstNames.getString("value")).isEqualTo("Joe");
        assertThat(firstNames.getString("nonLatinScriptValue")).isEqualTo("NonLatinJoe");

        JSONObject surnames = attributes.getJSONArray("surnames").getJSONObject(0);

        assertThat(surnames.getString("value")).isEqualTo("Bloggs");
        assertThat(surnames.getString("nonLatinScriptValue")).isEqualTo("NonLatinBloggs");
    }

    private Attribute anAttribute(String name) {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(name);
        return attribute;
    }

}
