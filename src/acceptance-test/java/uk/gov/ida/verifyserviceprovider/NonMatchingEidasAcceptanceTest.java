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
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_COUNTRY_TWO;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.aHubResponseContainingEidasUnsignedAssertions;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.anEidasResponseIssuedByACountry;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.aValidEidasResponse;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.anEidasResponseIssuedByACountryWithUnsignedAssertions;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.anInvalidAssertionSignatureEidasResponse;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.anInvalidSignatureEidasResponseIssuedByACountry;
import static uk.gov.ida.verifyserviceprovider.builders.AssertionHelper.getReEncryptedKeys;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class NonMatchingEidasAcceptanceTest {

    @ClassRule
    public static NonMatchingVerifyServiceProviderAppRule appWithoutEidasConfig = new NonMatchingVerifyServiceProviderAppRule();
    @ClassRule
    public static NonMatchingVerifyServiceProviderAppRule appWithEidasEnabled = new NonMatchingVerifyServiceProviderAppRule(true);
    @ClassRule
    public static NonMatchingVerifyServiceProviderAppRule appWithEidasDisabled = new NonMatchingVerifyServiceProviderAppRule(false);

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory;
    private final XmlObjectToBase64EncodedStringTransformer xmlToB64Transformer = new XmlObjectToBase64EncodedStringTransformer();

    @Before
    public void setUp() {
        openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    }

    @Test
     public void shouldReturn400WhenAssertionContainsAnInvalidSignature() throws MarshallingException, SignatureException {
         String base64Response = xmlToB64Transformer.apply(
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
         String base64Response = xmlToB64Transformer.apply(
                 aValidEidasResponse("requestId", STUB_COUNTRY_ONE).build()
         );
         Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
                 Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldReturn400ForEidasResponseWhenEuropeanIdentityConfigAbsent() throws Exception {
         String base64Response = xmlToB64Transformer.apply(
             aValidEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId()).build()
         );
         Response response = appWithoutEidasConfig.client().target(format("http://localhost:%s/translate-response", appWithoutEidasConfig.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldReturn400ForEidasResponseWhenEuropeanIdentityDisabled() throws Exception {
         String base64Response = xmlToB64Transformer.apply(
             aValidEidasResponse("requestId", appWithEidasDisabled.getCountryEntityId()).build()
         );
         Response response = appWithEidasDisabled.client().target(format("http://localhost:%s/translate-response", appWithEidasDisabled.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
     }

     @Test
     public void shouldProcessEidasResponseCorrectlyWhenEuropeanIdentityEnabled() throws Exception {
         String base64Response = xmlToB64Transformer.apply(
             aValidEidasResponse("requestId", appWithEidasEnabled.getCountryEntityId()).build()
         );
         Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
             Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
         );

         String body = response.readEntity(String.class);
         JSONObject json = new JSONObject(body);

         assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
         assertThat(json.getString("pid")).isNotEqualTo("default-pid");
         assertThat(json.getString("pid")).isEqualTo("428eb6096580250e9edbac60566529c2e8f9dbfe9ea88999b8996f6dbc602160");
     }

    @Test
    public void shouldHandleResponseFromHubContainingEidasUnsignedAssertions() throws Exception {
        org.opensaml.saml.saml2.core.Response countryResponse = anEidasResponseIssuedByACountryWithUnsignedAssertions("requestId", appWithEidasEnabled.getCountryEntityId()).build();
        org.opensaml.saml.saml2.core.Response hubResponse = aHubResponseContainingEidasUnsignedAssertions(
                "requestId",
                xmlToB64Transformer.apply(countryResponse),
                getReEncryptedKeys(countryResponse)
        ).build();

        Response appResponse = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(xmlToB64Transformer.apply(hubResponse), "requestId", LEVEL_2, null))
        );

        String body = appResponse.readEntity(String.class);
        JSONObject json = new JSONObject(body);

        assertThat(appResponse.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(json.getString("pid")).isEqualTo("428eb6096580250e9edbac60566529c2e8f9dbfe9ea88999b8996f6dbc602160");
    }

    @Test
    public void shouldReturn400ForCountryIssuedEidasResponseIfMetadataNotFoundInMetadataAggregatorWhenEuropeanIdentityEnabled() throws Exception {
        String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
                anEidasResponseIssuedByACountry("requestId", appWithEidasEnabled.getCountryEntityId())
                        .withIssuer(anIssuer().withIssuerId(STUB_COUNTRY_TWO).build())
                        .build()
        );
        Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
        );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void shouldReturn400ForCountryIssuedEidasResponseIfBadSignatureWhenEuropeanIdentityEnabled() throws Exception {
        String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(
                anInvalidSignatureEidasResponseIssuedByACountry("requestId", appWithEidasEnabled.getCountryEntityId())
                        .build()
        );
        Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
        );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void shouldMapAttributesCorrectly() throws Exception {
        AttributeStatementBuilder attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement();

        Attribute givenName = anAttribute(IdaConstants.Eidas_Attributes.FirstName.NAME);
        CurrentGivenName firstNameValue = new CurrentGivenNameBuilder().buildObject();
        firstNameValue.setFirstName("Joe");
        givenName.getAttributeValues().add(firstNameValue);
        CurrentGivenName nonLatinScriptFirstNameValue = new CurrentGivenNameBuilder().buildObject();
        nonLatinScriptFirstNameValue.setFirstName("NonLatinJoe");
        nonLatinScriptFirstNameValue.setIsLatinScript(false);
        givenName.getAttributeValues().add(nonLatinScriptFirstNameValue);

        attributeStatementBuilder.addAttribute(givenName);

        Attribute familyName = anAttribute(IdaConstants.Eidas_Attributes.FamilyName.NAME);

        CurrentFamilyName familyNameValue = new CurrentFamilyNameBuilder().buildObject();
        familyNameValue.setFamilyName("Bloggs");
        familyName.getAttributeValues().add(familyNameValue);

        CurrentFamilyName nonLatinFamilyNameValue = new CurrentFamilyNameBuilder().buildObject();
        nonLatinFamilyNameValue.setFamilyName("NonLatinBloggs");
        nonLatinFamilyNameValue.setIsLatinScript(false);
        familyName.getAttributeValues().add(nonLatinFamilyNameValue);

        attributeStatementBuilder.addAttribute(familyName);

        Attribute personIdentifier = anAttribute(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME);
        PersonIdentifier personIdentifierValue = new PersonIdentifierBuilder().buildObject();
        personIdentifierValue.setPersonIdentifier("JB12345");
        personIdentifier.getAttributeValues().add(personIdentifierValue);
        attributeStatementBuilder.addAttribute(personIdentifier);

        Attribute dateOfBirth = anAttribute(IdaConstants.Eidas_Attributes.DateOfBirth.NAME);
        DateOfBirth dateOfBirthValue = new DateOfBirthBuilder().buildObject();
        String dateOfBirthString = "1988-09-30";
        LocalDate now = LocalDate.parse(dateOfBirthString);
        dateOfBirthValue.setDateOfBirth(now);
        dateOfBirth.getAttributeValues().add(dateOfBirthValue);
        attributeStatementBuilder.addAttribute(dateOfBirth);

        org.opensaml.saml.saml2.core.Response samlResponse = aValidEidasResponse(
                "requestId",
                appWithEidasEnabled.getCountryEntityId(),
                attributeStatementBuilder.build())
                .build();

        String base64Response = new XmlObjectToBase64EncodedStringTransformer().apply(samlResponse);
        Response response = appWithEidasEnabled.client().target(format("http://localhost:%s/translate-response", appWithEidasEnabled.getLocalPort())).request().post(
                Entity.json(new TranslateSamlResponseBody(base64Response, "requestId", LEVEL_2, null))
        );

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        String body = response.readEntity(String.class);
        JSONObject json = new JSONObject(body);

        assertThat(json.has("pid")).isTrue();
        assertThat(json.getString("levelOfAssurance")).isEqualTo("LEVEL_2");

        JSONObject attributes = json.getJSONObject("attributes");

        JSONObject firstName = attributes.getJSONArray("firstNames").getJSONObject(0);
        assertThat(firstName.getString("value")).isEqualTo("Joe");
        assertThat(firstName.getString("nonLatinScriptValue")).isEqualTo("NonLatinJoe");
        assertThat(firstName.getBoolean("verified")).isTrue();
        assertThat(firstName.has("from")).isFalse();
        assertThat(firstName.has("to")).isFalse();

        JSONObject surname = attributes.getJSONArray("surnames").getJSONObject(0);
        assertThat(surname.getString("value")).isEqualTo("Bloggs");
        assertThat(surname.getString("nonLatinScriptValue")).isEqualTo("NonLatinBloggs");
        assertThat(surname.getBoolean("verified")).isTrue();
        assertThat(surname.has("from")).isFalse();
        assertThat(surname.has("to")).isFalse();

        JSONObject dateOfBirthAttribute = attributes.getJSONArray("datesOfBirth").getJSONObject(0);
        assertThat(dateOfBirthAttribute.getString("value")).isEqualTo(dateOfBirthString);
        assertThat(dateOfBirthAttribute.has("nonLatinScriptValue")).isFalse();
        assertThat(dateOfBirthAttribute.getBoolean("verified")).isTrue();
        assertThat(dateOfBirthAttribute.has("from")).isFalse();
        assertThat(dateOfBirthAttribute.has("to")).isFalse();
    }

    private Attribute anAttribute(String name) {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();
        attribute.setName(name);
        return attribute;
    }

}
