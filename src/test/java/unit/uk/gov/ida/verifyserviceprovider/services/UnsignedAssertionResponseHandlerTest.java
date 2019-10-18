package unit.uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.extensions.eidas.CountrySamlResponse;
import uk.gov.ida.saml.core.extensions.eidas.EncryptedAssertionKeys;
import uk.gov.ida.saml.core.extensions.eidas.impl.CountrySamlResponseBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.EncryptedAssertionKeysBuilder;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.EidasValidatorFactory;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SecretKeyDecryptorFactory;
import uk.gov.ida.saml.security.exception.SamlFailedToDecryptException;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.verifyserviceprovider.services.UnsignedAssertionsResponseHandler;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anEidasAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.ResponseBuilder.aResponse;

@RunWith(MockitoJUnitRunner.class)
public class UnsignedAssertionResponseHandlerTest {

    @Mock
    private EidasValidatorFactory eidasValidatorFactory;

    @Mock
    private StringToOpenSamlObjectTransformer<Response> stringToResponseTransformer;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private SecretKeyDecryptorFactory secretKeyDecryptorFactory;

    @Mock
    private Decrypter decrypter;

    @Mock
    private SamlAssertionsSignatureValidator hubAssertionSignatureValidator;

    private UnsignedAssertionsResponseHandler handler;
    private final List<String> singleKeyList = Arrays.asList("aKey");
    private final String samlString = "eidasSaml";
    private final String inResponseTo = "inResponseTo";
    private ValidatedResponse validatedResponse;
    private Response eidasResponse;

    @Before
    public void setUp() throws Exception {
        IdaSamlBootstrap.bootstrap();

        handler = new UnsignedAssertionsResponseHandler(
                eidasValidatorFactory,
                stringToResponseTransformer,
                instantValidator,
                secretKeyDecryptorFactory,
                getEncryptionAlgorithmValidator(),
                hubAssertionSignatureValidator
        );
        eidasResponse = createEidasResponse();
        validatedResponse = new ValidatedResponse(eidasResponse);
    }

    @Test
    public void getValidatedResponseShouldValidateResponse() {
        List<Assertion> eidasSamlAssertion = Arrays.asList(anEidasSamlAssertion(singleKeyList));

        when(hubAssertionSignatureValidator.validate(eidasSamlAssertion, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(new ValidatedAssertions(eidasSamlAssertion));
        when(stringToResponseTransformer.apply(samlString)).thenReturn(eidasResponse);
        when(eidasValidatorFactory.getValidatedResponse(eidasResponse)).thenReturn(validatedResponse);

        handler.getValidatedResponse(eidasSamlAssertion, inResponseTo);

        verify(hubAssertionSignatureValidator).validate(eidasSamlAssertion, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        verify(stringToResponseTransformer).apply(samlString);
        verify(eidasValidatorFactory).getValidatedResponse(eidasResponse);
        verify(instantValidator).validate(validatedResponse.getIssueInstant(), "Response IssueInstant");
    }

    @Test
    public void getValidatedResponseShouldThrowIfInResponseToIsNotExpected() {
        List<Assertion> eidasSamlAssertion = Arrays.asList(anEidasSamlAssertion(singleKeyList));

        when(hubAssertionSignatureValidator.validate(eidasSamlAssertion, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(new ValidatedAssertions(eidasSamlAssertion));
        when(stringToResponseTransformer.apply(samlString)).thenReturn(eidasResponse);
        when(eidasValidatorFactory.getValidatedResponse(eidasResponse)).thenReturn(validatedResponse);

        assertThrows(SamlResponseValidationException.class, () -> {
            handler.getValidatedResponse(eidasSamlAssertion, "thisIsNotTheResponseIdYouAreLookingFor");
        });
    }

    @Test
    public void decryptAssertionShouldDecryptWithCorrectKey() throws Exception {
        Assertion eidasSamlAssertion = anEidasSamlAssertion(singleKeyList);
        Assertion expectedAssertion = anEidasAssertion().buildUnencrypted();

        when(secretKeyDecryptorFactory.createDecrypter(singleKeyList.get(0))).thenReturn(decrypter);
        when(decrypter.decrypt(any(EncryptedAssertion.class))).thenReturn(expectedAssertion);
        List<Assertion> assertions = handler.decryptAssertion(validatedResponse, eidasSamlAssertion);

        assertThat(assertions.size()).isEqualTo(1);
        assertThat(assertions.get(0)).isEqualTo(expectedAssertion);
    }

    @Test
    public void decryptAssertionShouldTryMultipleKeys() throws Exception {
        Assertion eidasSamlAssertion = anEidasSamlAssertion(Arrays.asList("wrongKey", "anotherWrongKey", "theCorretKey"));
        Assertion expectedAssertion = anEidasAssertion().buildUnencrypted();

        when(secretKeyDecryptorFactory.createDecrypter("theCorretKey")).thenReturn(decrypter);
        when(decrypter.decrypt(any(EncryptedAssertion.class))).thenReturn(expectedAssertion);
        List<Assertion> assertions = handler.decryptAssertion(validatedResponse, eidasSamlAssertion);

        verify(secretKeyDecryptorFactory, times(3)).createDecrypter(any());

        assertThat(assertions.size()).isEqualTo(1);
        assertThat(assertions.get(0)).isEqualTo(expectedAssertion);
    }

    @Test
    public void decryptAssertionShouldThrowIfNoKeysCanDecrypt() throws Exception {
        Assertion eidasSamlAssertion = anEidasSamlAssertion(Arrays.asList("wrongKey", "anotherWrongKey", "ohNo!"));

        assertThrows(SamlFailedToDecryptException.class, () -> {
            handler.decryptAssertion(validatedResponse, eidasSamlAssertion);
        });

        verify(secretKeyDecryptorFactory, times(3)).createDecrypter(any());
    }

    @Test
    public void decryptAssertionShouldThrowIfWrongEncryptionAlgorithmUsed() throws Exception {
        handler = new UnsignedAssertionsResponseHandler(
                eidasValidatorFactory,
                stringToResponseTransformer,
                instantValidator,
                secretKeyDecryptorFactory,
                new EncryptionAlgorithmValidator(
                        ImmutableSet.of(
                                EncryptionConstants.ALGO_ID_BLOCKCIPHER_TRIPLEDES
                        ),
                        ImmutableSet.of(
                                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP
                        )
                ),
                hubAssertionSignatureValidator
        );
        Assertion eidasSamlAssertion = anEidasSamlAssertion(singleKeyList);

        assertThrows(SamlFailedToDecryptException.class, () -> {
            handler.decryptAssertion(validatedResponse, eidasSamlAssertion);
        });
    }

    private Assertion anEidasSamlAssertion(List<String> keys) {
        return anAssertion()
                .addAttributeStatement(
                        anAttributeStatement()
                                .addAttribute(createCountrySamlResponseAttribute(samlString))
                                .addAttribute(createEncryptedAssertionKeysAttribute(keys))
                                .build())
                .buildUnencrypted();
    }

    private Attribute createCountrySamlResponseAttribute(String countrySaml) {
        CountrySamlResponse attributeValue = new CountrySamlResponseBuilder().buildObject();
        attributeValue.setValue(countrySaml);

        Attribute attribute = (Attribute) XMLObjectSupport.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);
        attribute.setFriendlyName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.FRIENDLY_NAME);
        attribute.setNameFormat(Attribute.URI_REFERENCE);

        attribute.getAttributeValues().add(attributeValue);
        return attribute;
    }

    private Attribute createEncryptedAssertionKeysAttribute(List<String> keys) {
        List<EncryptedAssertionKeys> assertionKeysValues = new ArrayList<>();
        for (String key : keys) {
            EncryptedAssertionKeys attributeValue = new EncryptedAssertionKeysBuilder().buildObject();
            attributeValue.setValue(key);
            assertionKeysValues.add(attributeValue);
        }

        Attribute attribute = (Attribute) XMLObjectSupport.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.NAME);
        attribute.setFriendlyName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.FRIENDLY_NAME);
        attribute.setNameFormat(Attribute.URI_REFERENCE);

        attribute.getAttributeValues().addAll(assertionKeysValues);
        return attribute;
    }

    private Response createEidasResponse() throws Exception {
        return aResponse()
            .addEncryptedAssertion(
                    anEidasAssertion().build())
            .withInResponseTo(inResponseTo)
            .build();
    }

    private EncryptionAlgorithmValidator getEncryptionAlgorithmValidator() {
        return new EncryptionAlgorithmValidator(
                ImmutableSet.of(
                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128
                ),
                ImmutableSet.of(
                        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP
                )
        );
    }
}
