package uk.gov.ida.verifyserviceprovider.builders;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

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
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anEidasAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class AssertionHelper {

    public static EncryptedAssertion anEidasEncryptedAssertion(String requestId, String issuerId, Signature assertionSignature) {
        return anEidasEncryptedAssertion(requestId, issuerId, assertionSignature, anEidasAttributeStatement().build());
    }

    public static EncryptedAssertion anEidasEncryptedAssertion(String requestId,
                                                               String issuerId,
                                                               Signature assertionSignature,
                                                               AttributeStatement attributeStatement) {
        return anAssertion()
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
                .withSignature(assertionSignature)
                .withConditions(aConditions())
                .buildWithEncrypterCredential(
                        new TestCredentialFactory(
                                TEST_RP_PUBLIC_ENCRYPTION_CERT,
                                TEST_RP_PRIVATE_ENCRYPTION_KEY
                        ).getEncryptingCredential()
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
        AudienceRestriction audienceRestriction= new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(TEST_RP);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }
}
