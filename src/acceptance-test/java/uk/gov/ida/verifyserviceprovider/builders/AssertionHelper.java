package uk.gov.ida.verifyserviceprovider.builders;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.PublicKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.eidas.CountrySamlResponse;
import uk.gov.ida.saml.core.extensions.eidas.EncryptedAssertionKeys;
import uk.gov.ida.saml.core.extensions.eidas.impl.CountrySamlResponseBuilder;
import uk.gov.ida.saml.core.extensions.eidas.impl.EncryptedAssertionKeysBuilder;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.KeyStoreBackedEncryptionCredentialResolver;
import uk.gov.ida.saml.security.SecretKeyEncrypter;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anEidasAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.verifyserviceprovider.rules.NonMatchingVerifyServiceProviderAppRule.COUNTRY_ENTITY_ID;

public class AssertionHelper {

    public static EncryptedAssertion anEidasEncryptedAssertion(String requestId, String issuerId, Signature assertionSignature) {
        return anEidasEncryptedAssertion(requestId, issuerId, assertionSignature, anEidasAttributeStatement().build());
    }

    public static EncryptedAssertion anUnsignedEidasEncryptedAssertion(String requestId,
                                                                       String issuerId,
                                                                       Signature assertionSignature) {
        return anEidasEncryptedAssertion(
                requestId,
                issuerId,
                assertionSignature,
                anEidasAttributeStatement().build(),
                false,
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM
        );
    };

    public static EncryptedAssertion anEidasEncryptedAssertion(String requestId,
                                                               String issuerId,
                                                               Signature assertionSignature,
                                                               AttributeStatement attributeStatement) {
        return anEidasEncryptedAssertion(
                requestId,
                issuerId,
                assertionSignature,
                attributeStatement,
                true,
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128
        );
    }

    public static EncryptedAssertion anEidasEncryptedAssertion(String requestId,
                                                               String issuerId,
                                                               Signature assertionSignature,
                                                               AttributeStatement attributeStatement,
                                                               boolean shouldSign,
                                                               String encryptionAlgorithm)
    {
        AssertionBuilder assertionBuilder = anAssertion()
                .withSubject(
                        aSubject().withSubjectConfirmation(
                                aSubjectConfirmation().withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withInResponseTo(requestId)
                                                .build())
                                        .build())
                                .build())
                .withIssuer(
                        anIssuer()
                                .withIssuerId(issuerId)
                                .build())
                .addAttributeStatement(attributeStatement)
                .addAuthnStatement(anEidasAuthnStatement().build())
                .withConditions(aConditionsForEidas());

        if (shouldSign) {
            assertionBuilder.withSignature(assertionSignature);
        } else {
            assertionBuilder.withoutSigning();
            assertionBuilder.withSignature(null);
        }

        return assertionBuilder.buildWithEncrypterCredential(
                new TestCredentialFactory(
                        TEST_RP_PUBLIC_ENCRYPTION_CERT,
                        TEST_RP_PRIVATE_ENCRYPTION_KEY
                ).getEncryptingCredential(),
                encryptionAlgorithm
        );
    }



    public static EncryptedAssertion anEidasEncryptedAssertionWithInvalidSignature(String assertionIssuerId) {
        return anAssertion()
            .addAuthnStatement(AuthnStatementBuilder.anAuthnStatement().build())
            .withIssuer(
                anIssuer()
                    .withIssuerId(assertionIssuerId)
                    .build())
            .withSignature(aSignature()
                .withSigningCredential(
                    new TestCredentialFactory(
                        TEST_RP_PUBLIC_SIGNING_CERT,
                        TEST_RP_PRIVATE_SIGNING_KEY
                    ).getSigningCredential()
                ).build())
            .withConditions(aConditions())
            .buildWithEncrypterCredential(
                new TestCredentialFactory(
                    TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
                    TEST_RP_MS_PRIVATE_ENCRYPTION_KEY
                ).getEncryptingCredential()
            );
    }

    public static ResponseBuilder aValidEidasResponse(String requestId, String assertionIssuerId, AttributeStatement attributeStatement) {
        return ResponseBuilder.aResponse()
                .withId(requestId)
                .withInResponseTo(requestId)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .addEncryptedAssertion(anEidasEncryptedAssertion(requestId, assertionIssuerId, anEidasSignature(), attributeStatement))
                .withSigningCredential(
                        new TestCredentialFactory(
                                HUB_TEST_PUBLIC_SIGNING_CERT,
                                HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential());
    }

    public static ResponseBuilder aValidEidasResponse(String requestId, String assertionIssuerId) {
        return aValidEidasResponse(requestId, assertionIssuerId, anEidasAttributeStatement().build());
    }

    public static ResponseBuilder anInvalidSignatureEidasResponse(String requestId, String assertionIssuerId) {
        return ResponseBuilder.aResponse()
                .withId(requestId)
                .withInResponseTo(requestId)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .addEncryptedAssertion(anEidasEncryptedAssertion(requestId, assertionIssuerId, anEidasSignature()))
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                                STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential());
    }

    public static ResponseBuilder anEidasResponseIssuedByACountry(String requestId, String assertionIssuerId) {
        return ResponseBuilder.aResponse()
                .withId(requestId)
                .withIssuer(anIssuer().withIssuerId(COUNTRY_ENTITY_ID).build())
                .withInResponseTo(requestId)
                .addEncryptedAssertion(anEidasEncryptedAssertion(requestId, assertionIssuerId, anEidasSignature()))
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                                STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential());
    }

    public static ResponseBuilder anEidasResponseIssuedByACountryWithUnsignedAssertions(String requestId, String assertionIssuerId) {
        return ResponseBuilder.aResponse()
                .withId(requestId)
                .withIssuer(anIssuer().withIssuerId(COUNTRY_ENTITY_ID).build())
                .withInResponseTo(requestId)
                .addEncryptedAssertion(anUnsignedEidasEncryptedAssertion(requestId, assertionIssuerId, anEidasSignature()))
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                                STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential());
    }

    public static ResponseBuilder aHubResponseContainingEidasUnsignedAssertions(
            String requestId,
            String countrySamlResponseString,
            List<String> encryptedKeys
    ) {
        return ResponseBuilder.aResponse()
                .withId(requestId)
                .withInResponseTo(requestId)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .addEncryptedAssertion(
                        anEidasEncryptedAssertion(
                                requestId,
                                HUB_ENTITY_ID,
                                aHubSignature(),
                                anAttributeStatementContainingAnEidasUnsignedResponse(
                                        countrySamlResponseString,
                                        encryptedKeys
                                )
                        ))
                .withSigningCredential(
                        new TestCredentialFactory(
                                HUB_TEST_PUBLIC_SIGNING_CERT,
                                HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential());
    }

    public static ResponseBuilder anInvalidSignatureEidasResponseIssuedByACountry(String requestId, String assertionIssuerId) {
        return ResponseBuilder.aResponse()
                .withId(requestId)
                .withInResponseTo(requestId)
                .withIssuer(anIssuer().withIssuerId(COUNTRY_ENTITY_ID).build())
                .addEncryptedAssertion(anEidasEncryptedAssertion(requestId, assertionIssuerId, anEidasSignature()))
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_IDP_PUBLIC_PRIMARY_CERT,
                                STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential());
    }

    public static ResponseBuilder anInvalidAssertionSignatureEidasResponse(String requestId, String assertionIssuerId) {
        return ResponseBuilder.aResponse()
                .withId(requestId)
                .withInResponseTo(requestId)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .addEncryptedAssertion(anEidasEncryptedAssertion(requestId, assertionIssuerId, anIdpSignature()))
                .withSigningCredential(
                        new TestCredentialFactory(
                                HUB_TEST_PUBLIC_SIGNING_CERT,
                                HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential());
    }

    public static Signature anEidasSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                                STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential()
                ).build();
    }

    public static Signature anIdpSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_IDP_PUBLIC_PRIMARY_CERT,
                                STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential()
                ).build();
    }

    public static Signature aHubSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                HUB_TEST_PUBLIC_SIGNING_CERT,
                                HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                ).build();
    }

    private static Subject anAssertionSubject(final String inResponseTo, boolean shouldBeExpired) {
        final DateTime notOnOrAfter;
        if (shouldBeExpired) {
            notOnOrAfter = DateTime.now().minusMinutes(5);
        } else {
            notOnOrAfter = DateTime.now().plus(1000000);
        }
        return aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withNotOnOrAfter(notOnOrAfter)
                                                .withInResponseTo(inResponseTo)
                                                .build()
                                ).build()
                ).build();
    }

    private static Conditions aConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(DateTime.now());
        conditions.setNotOnOrAfter(DateTime.now().plusMinutes(10));
        AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(TEST_RP);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private static Conditions aConditionsForEidas() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(DateTime.now());
        conditions.setNotOnOrAfter(DateTime.now().plusMinutes(10));
        AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(HUB_CONNECTOR_ENTITY_ID);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private static AttributeStatement anAttributeStatementContainingAnEidasUnsignedResponse(String countrySamlResponseValue, List<String> encryptedKeys) {
        CountrySamlResponse countrySamlAttributeValue = new CountrySamlResponseBuilder().buildObject();
        countrySamlAttributeValue.setValue(countrySamlResponseValue);

        Attribute countrySamlAttribute = (Attribute) XMLObjectSupport.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        countrySamlAttribute.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME);
        countrySamlAttribute.setFriendlyName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.FRIENDLY_NAME);
        countrySamlAttribute.setNameFormat(Attribute.URI_REFERENCE);

        countrySamlAttribute.getAttributeValues().add(countrySamlAttributeValue);

        List<EncryptedAssertionKeys> assertionKeysValues = new ArrayList<>();
        for (String key : encryptedKeys) {
            EncryptedAssertionKeys keysAttribtueValue = new EncryptedAssertionKeysBuilder().buildObject();
            keysAttribtueValue.setValue(key);
            assertionKeysValues.add(keysAttribtueValue);
        }

        Attribute keysAttribute = (Attribute) XMLObjectSupport.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
        keysAttribute.setName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.NAME);
        keysAttribute.setFriendlyName(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.FRIENDLY_NAME);
        keysAttribute.setNameFormat(Attribute.URI_REFERENCE);

        keysAttribute.getAttributeValues().addAll(assertionKeysValues);

        return anAttributeStatement()
                .addAttribute(countrySamlAttribute)
                .addAttribute(keysAttribute)
                .build();
    }

    public static List<String> getReEncryptedKeys(Response countryResponse) {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(TEST_RP)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.getPrimaryPublicEncryptionCert(TEST_RP));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TEST_RP_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(
                new IdaKeyStore(new KeyPair(publicKey, privateKey), Arrays.asList(encryptionKeyPair))
        );
        List<Credential> credentials = keyStoreCredentialRetriever.getDecryptingCredentials();
        Decrypter decrypter = new DecrypterFactory().createDecrypter(credentials);
        AssertionDecrypter assertionDecrypter = new AssertionDecrypter(new EncryptionAlgorithmValidator(), decrypter);

        KeyStoreBackedEncryptionCredentialResolver credentialResolver = mock(KeyStoreBackedEncryptionCredentialResolver.class);
        Credential credential = new TestCredentialFactory(TEST_RP_PUBLIC_ENCRYPTION_CERT, null).getEncryptingCredential();
        when(credentialResolver.getEncryptingCredential(TEST_RP)).thenReturn(credential);
        SecretKeyEncrypter secretKeyEncrypter = new SecretKeyEncrypter(credentialResolver);

        return assertionDecrypter.getReEncryptedKeys(new ValidatedResponse(countryResponse), secretKeyEncrypter, TEST_RP);
    }
}
