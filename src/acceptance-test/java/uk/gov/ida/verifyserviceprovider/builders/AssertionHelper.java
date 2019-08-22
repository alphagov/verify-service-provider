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
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;

import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_COUNTRY_ONE;
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

    public static Credential anEncryptingCredentialForTestRP() {
        return new TestCredentialFactory(TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_RP_PRIVATE_ENCRYPTION_KEY).getEncryptingCredential();
    }

    public static EncryptedAssertion anEidasEncryptedAssertion(String requestId, String issuerId, Signature assertionSignature) {
        return anEidasEncryptedAssertionForTheTestRP(requestId, issuerId, assertionSignature, anEidasAttributeStatement().build());
    }

    public static EncryptedAssertion anEidasEncryptedAssertionForTheTestRP(String requestId, String issuerId, Signature assertionSignature, AttributeStatement attributeStatement) {
        return anEidasEncryptedAssertion(requestId, issuerId, assertionSignature, attributeStatement, anEncryptingCredentialForTestRP());
    }

    public static EncryptedAssertion anEidasEncryptedAssertion(String requestId,
                                                               String issuerId,
                                                               Signature assertionSignature,
                                                               AttributeStatement attributeStatement,
                                                               Credential target) {
        return anAssertion()
                .withSubject(
                        aSubject().withSubjectConfirmation(
                                aSubjectConfirmation().withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withInResponseTo(requestId)
                                                .build())
                                        .build())
                                .build())
                .withIssuer(anIssuer().withIssuerId(issuerId).build())
                .addAttributeStatement(attributeStatement)
                .addAuthnStatement(anEidasAuthnStatement().build())
                .withSignature(assertionSignature)
                .withConditions(aConditionsForEidas())
                .buildWithEncrypterCredential(target);
    }

    public static ResponseBuilder aValidEidasresponseSignedByHubWithAnAssertionSignedByStubCountry(String requestId, String assertionIssuerId) {
        return aValidEidasresponseSignedByHubWithAnAssertionSignedByStubCountry(requestId, assertionIssuerId, anEidasAttributeStatement().build());
    }

    public static ResponseBuilder aValidEidasresponseSignedByHubWithAnAssertionSignedByStubCountry(String requestId, String assertionIssuerId, AttributeStatement attributeStatement) {
        return ResponseBuilder.aResponse()
                .withId(requestId)
                .withInResponseTo(requestId)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .addEncryptedAssertion(anEidasEncryptedAssertionForTheTestRP(requestId, assertionIssuerId, aStubCountryEidasSignature(), attributeStatement))
                .withSigningCredential(
                        new TestCredentialFactory(
                                HUB_TEST_PUBLIC_SIGNING_CERT,
                                HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential());
    }

    public static ResponseBuilder aValidEidasResponseSignedByStubCountryWithAnAssertionSignedByTheCountry(String requestId) {
        return ResponseBuilder.aResponse()
            .withId(requestId)
            .withInResponseTo(requestId)
            .withIssuer(anIssuer().withIssuerId(STUB_COUNTRY_ONE).build())
            .addEncryptedAssertion(anEidasEncryptedAssertion(requestId, STUB_COUNTRY_ONE, aStubCountryEidasSignature()))
            .withSigningCredential(
                new TestCredentialFactory(
                    STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                    STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                ).getSigningCredential());
    }

    public static ResponseBuilder aValidEidasResponseSignedByStubCountryWithAnAssertionSignedByTheCountry(String requestId, AttributeStatementBuilder attributeStatementBuilder) {
        return ResponseBuilder.aResponse()
            .withId(requestId)
            .withInResponseTo(requestId)
            .withIssuer(anIssuer().withIssuerId(STUB_COUNTRY_ONE).build())
            .addEncryptedAssertion(anEidasEncryptedAssertionForTheTestRP(requestId, STUB_COUNTRY_ONE, aStubCountryEidasSignature(), attributeStatementBuilder.build()))
            .withSigningCredential(
                new TestCredentialFactory(
                    STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                    STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                ).getSigningCredential());
    }

    @Deprecated
    public static ResponseBuilder aValidEidasResponseSignedByHubWithAnAssertionSignedByAnIDP(String requestId) {
        return ResponseBuilder.aResponse()
            .withId(requestId)
            .withInResponseTo(requestId)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .addEncryptedAssertion(anEidasEncryptedAssertion(requestId, HUB_ENTITY_ID, anIdpSignature()))
            .withSigningCredential(
                new TestCredentialFactory(
                    HUB_TEST_PUBLIC_SIGNING_CERT,
                    HUB_TEST_PRIVATE_SIGNING_KEY
                ).getSigningCredential());
    }

    public static ResponseBuilder aValidEidasResponseSignedByStubCountryWithAnInvalidSignatureOnTheAssertion(String requestId) {
        return ResponseBuilder.aResponse()
            .withId(requestId)
            .withInResponseTo(requestId)
            .withIssuer(anIssuer().withIssuerId(STUB_COUNTRY_ONE).build())
            .addEncryptedAssertion(anEidasEncryptedAssertion(requestId, STUB_COUNTRY_ONE, aBadStubCountryEidasSignature()))
            .withSigningCredential(
                new TestCredentialFactory(
                    STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                    STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                ).getSigningCredential());
    }

    public static Signature aBadStubCountryEidasSignature() {
        return aSignature()
            .withSigningCredential(
                new TestCredentialFactory(
                    STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                    HUB_CONNECTOR_TEST_PRIVATE_SIGNING_KEY // mismatch
                ).getSigningCredential())
            .build();
    }

    public static Signature aStubCountryEidasSignature() {
        return aSignature()
            .withSigningCredential(
                new TestCredentialFactory(
                    STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                    STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                ).getSigningCredential())
            .build();
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

    private static Conditions aConditionsForEidas() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(DateTime.now());
        conditions.setNotOnOrAfter(DateTime.now().plusMinutes(10));
        AudienceRestriction audienceRestriction= new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(HUB_CONNECTOR_ENTITY_ID);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }
}
