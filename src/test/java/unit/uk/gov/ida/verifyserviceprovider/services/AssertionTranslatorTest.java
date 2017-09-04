package unit.uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.OneTimeUseBuilder;
import org.opensaml.saml.saml2.core.impl.ProxyRestrictionBuilder;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.test.PrivateKeyStoreFactory;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.AuthnContextBuilder;
import uk.gov.ida.saml.core.test.builders.ConditionsBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectBuilder;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.services.AssertionTranslator;

import java.security.PrivateKey;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.extensions.IdaAuthnContext.LEVEL_1_AUTHN_CTX;
import static uk.gov.ida.saml.core.extensions.IdaAuthnContext.LEVEL_2_AUTHN_CTX;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AudienceRestrictionBuilder.anAudienceRestriction;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.ConditionsBuilder.aConditions;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder.anEntityDescriptor;
import static uk.gov.ida.saml.core.test.builders.metadata.IdpSsoDescriptorBuilder.anIdpSsoDescriptor;
import static uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder.aKeyDescriptor;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.Scenario.SUCCESS_MATCH;

public class AssertionTranslatorTest {

    private static final String IN_RESPONSE_TO = "_some-request-id";
    private static final String VERIFY_SERVICE_PROVIDER_ENTITY_ID = "default-entity-id";
    private AssertionTranslator translator;
    private Credential testRpMsaSigningCredential =
        new TestCredentialFactory(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY).getSigningCredential();

    @Before
    public void setUp() throws Exception {
        PrivateKey privateKey = new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP).getEncryptionPrivateKeys().get(0);
        ResponseFactory responseFactory = new ResponseFactory(VERIFY_SERVICE_PROVIDER_ENTITY_ID, privateKey, privateKey);

        EntityDescriptor entityDescriptor = anEntityDescriptor()
            .withIdpSsoDescriptor(anIdpSsoDescriptor()
                .addKeyDescriptor(aKeyDescriptor()
                    .withX509ForSigning(TEST_RP_MS_PUBLIC_SIGNING_CERT)
                    .build())
                .build())
            .build();

        MetadataResolver msaMetadataResolver = mock(MetadataResolver.class);
        when(msaMetadataResolver.resolve(any())).thenReturn(ImmutableList.of(entityDescriptor));

        translator = responseFactory.createAssertionTranslator(msaMetadataResolver);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldTranslateValidAssertion() {
        TranslatedResponseBody result = translator.translate(ImmutableList.of(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).buildUnencrypted()
        ), IN_RESPONSE_TO, LEVEL_2);
        assertThat(result).isEqualTo(new TranslatedResponseBody(
            SUCCESS_MATCH,
            "some-pid",
            LEVEL_2,
            null
        ));
    }

    @Test
    public void shouldAllowHigherLevelOfAssuranceThanRequested() throws Exception {
        TranslatedResponseBody result = translator.translate(ImmutableList.of(
                anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).buildUnencrypted()
        ), IN_RESPONSE_TO, LEVEL_1);
        assertThat(result).isEqualTo(new TranslatedResponseBody(
                SUCCESS_MATCH,
                "some-pid",
                LEVEL_2,
                null
        ));
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsIsEmptyList() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one assertion is expected.");

        translator.translate(Collections.emptyList(), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsIsNull() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one assertion is expected.");

        translator.translate(null, IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsListSizeIsLargerThanOne() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one assertion is expected.");

        translator.translate(
            ImmutableList.of(
                anAssertion().buildUnencrypted(),
                anAssertion().buildUnencrypted()
            ),
            IN_RESPONSE_TO,
                LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenAssertionIsNotSigned() throws Exception {
        expectedException.expect(SamlTransformationErrorException.class);
        expectedException.expectMessage("SAML Validation Specification: Message signature is not signed");

        translator.translate(Collections.singletonList(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).withoutSigning().buildUnencrypted()),
            IN_RESPONSE_TO,
                LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenAssertionSignedByUnknownKey() throws Exception {
        expectedException.expect(SamlTransformationErrorException.class);
        expectedException.expectMessage("SAML Validation Specification: Signature was not valid.");

        Credential unknownSigningCredential = new TestCredentialFactory(TEST_PUBLIC_CERT, TEST_PRIVATE_KEY).getSigningCredential();
        translator.translate(Collections.singletonList(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX)
                .withSignature(aSignature().withSigningCredential(unknownSigningCredential).build())
                .buildUnencrypted()),
            IN_RESPONSE_TO,
                LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectIsMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject is missing from the assertion.");

        Assertion assertion = aSignedAssertion()
            .withSubject(null)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenMultipleSubjectConfirmation() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one subject confirmation is expected.");

        Subject subject = aSubject().build();

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().build();
        subject.getSubjectConfirmations().addAll(ImmutableList.of(subjectConfirmation, subjectConfirmation));

        Assertion assertion = aSignedAssertion()
            .withSubject(subject)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationMethodIsNotBearer() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject confirmation method must be 'bearer'.");

        Assertion assertion = aSignedAssertion()
            .withSubject(
                aSubject()
                    .withSubjectConfirmation(aSubjectConfirmation().withMethod("anything-but-not-bearer").build())
                    .build()
            )
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject confirmation data is missing from the assertion.");

        Assertion assertion = aSignedAssertion()
            .withSubject(
                aSubject()
                    .withSubjectConfirmation(aSubjectConfirmation().withSubjectConfirmationData(null).build())
                    .build()
            )
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataNotBeforeIsAfterTheCurrentTime() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        DateTime notBefore = DateTime.now().plusYears(50);
        expectedException.expectMessage("Assertion is not valid before " + notBefore.withZone(UTC).toString(dateHourMinuteSecond()));

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData().withNotBefore(notBefore).build()
        ).build();

        Assertion assertion = aSignedAssertion()
            .withSubject(
                aSubject()
                    .withSubjectConfirmation(subjectConfirmation)
                    .build()
            )
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterIsMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject confirmation data must contain 'NotOnOrAfter'.");

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData().withNotOnOrAfter(null).build()
        ).build();

        Assertion assertion = aSignedAssertion()
            .withSubject(
                aSubject()
                    .withSubjectConfirmation(subjectConfirmation)
                    .build()
            )
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenNotOnOrAfterIsBeforeNow() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion is not valid on or after ");

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData().withNotOnOrAfter(DateTime.now().minusYears(50)).build()
        ).build();

        Assertion assertion = aSignedAssertion()
            .withSubject(
                aSubject()
                    .withSubjectConfirmation(subjectConfirmation)
                    .build()
            )
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenNotOnOrAfterIsTooFarInTheFuture() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("NotOnOrAfter is too far into the future ");

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(aSubjectConfirmationData()
                        .withNotOnOrAfter(DateTime.now().plusMinutes(200))
                        .build())
                .build();

        Assertion assertion = aSignedAssertion()
                .withSubject(aSubject()
                        .withSubjectConfirmation(subjectConfirmation)
                        .build())
                .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenAuthnInstantIsInFuture() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("AuthnInstant is in the future ");

        Assertion assertion = aSignedAssertion()
                .addAuthnStatement(anAuthnStatement()
                    .withAuthnInstant(DateTime.now().plusMinutes(10))
                    .build())
                .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataHasNoInResponseTo() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject confirmation data must contain 'InResponseTo'.");

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .withInResponseTo(null)
                .build()
        ).build();

        Assertion assertion = aSignedAssertion()
            .withSubject(
                aSubject()
                    .withSubjectConfirmation(subjectConfirmation)
                    .build()
            )
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenInResponseToRequestIdDoesNotMatchTheRequestId() throws Exception {
        String expectedInResponseTo = "some-non-matching-request-id";
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("'InResponseTo' must match requestId. Expected " + expectedInResponseTo + " but was " + IN_RESPONSE_TO);

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .withInResponseTo(IN_RESPONSE_TO)
                .build()
        ).build();

        Assertion assertion = aSignedAssertion()
            .withSubject(
                aSubject()
                    .withSubjectConfirmation(subjectConfirmation)
                    .build()
            )
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), expectedInResponseTo, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenNameIdIsMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("NameID is missing from the subject of the assertion.");

        Assertion assertion = aSignedAssertion()
            .withSubject(aValidSubject().withNameId(null).build())
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsIsMissing() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Conditions is missing from the assertion.");

        Assertion assertion = aSignedAssertion()
            .withConditions(null)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenNowIsBeforeConditionsNotBefore() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion is not valid before ");

        Conditions conditionsElement = aConditions().build();
        conditionsElement.setNotBefore(DateTime.now().plusYears(50));

        Assertion assertion = aSignedAssertion()
            .withConditions(conditionsElement)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenNowIsAfterConditionsNotOnOrAfter() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion is not valid on or after ");

        Conditions conditionsElement = aConditions().build();
        conditionsElement.setNotOnOrAfter(DateTime.now().minusYears(50));

        Assertion assertion = aSignedAssertion()
            .withConditions(conditionsElement)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenIssueInstantIsTooOld() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion IssueInstant is too far in the past ");

        Assertion assertion = aSignedAssertion()
                .withIssueInstant(DateTime.now().minusMinutes(10))
                .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenIssueInstantIsInTheFuture() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion IssueInstant is in the future ");

        Assertion assertion = aSignedAssertion()
                .withIssueInstant(DateTime.now().plusMinutes(1))
                .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsContainsProxyRestriction() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Conditions should not contain proxy restriction element.");


        Conditions conditionsElement = aConditions().build();
        conditionsElement.getConditions().add(new ProxyRestrictionBuilder().buildObject());

        Assertion assertion = aSignedAssertion()
            .withConditions(conditionsElement)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsContainsOneTimeUse() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Conditions should not contain one time use element.");

        Conditions conditionsElement = aConditions().build();
        conditionsElement.getConditions().add(new OneTimeUseBuilder().buildObject());

        Assertion assertion = aSignedAssertion()
            .withConditions(conditionsElement)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenAudienceRestrictionMissing() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one audience restriction is expected.");

        Conditions conditionsElement = aConditions().withoutDefaultAudienceRestriction().build();

        Assertion assertion = aSignedAssertion()
            .withConditions(conditionsElement)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenMultipleAudiencesInAudienceRestriction() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one audience is expected.");

        AudienceRestriction audienceRestriction = anAudienceRestriction().build();
        audienceRestriction.getAudiences().add(new AudienceBuilder().buildObject());
        audienceRestriction.getAudiences().add(new AudienceBuilder().buildObject());

        Conditions conditionsElement = aConditions()
            .withoutDefaultAudienceRestriction()
            .addAudienceRestriction(audienceRestriction)
            .build();

        Assertion assertion = aSignedAssertion()
            .withConditions(conditionsElement)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenAudienceRestrictionDoesNotMatchEntityId() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Audience must match entity ID. Expected " + VERIFY_SERVICE_PROVIDER_ENTITY_ID + " but was some-entity-id");

        Conditions conditionsElement = aConditions()
            .withoutDefaultAudienceRestriction()
            .addAudienceRestriction(
                anAudienceRestriction()
                    .withAudienceId("some-entity-id")
                    .build())
            .build();

        Assertion assertion = aSignedAssertion()
            .withConditions(conditionsElement)
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenAuthnStatementsIsEmpty() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one authn statement is expected.");

        Assertion assertion = aSignedAssertion()
            .buildUnencrypted();
        assertion.getAuthnStatements().clear();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenMultipleAuthnStatementsPresent() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one authn statement is expected.");

        Assertion assertion = aSignedAssertion()
            .addAuthnStatement(anAuthnStatement().build())
            .addAuthnStatement(anAuthnStatement().build())
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWhenLevelOfAssuranceNotPresent() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Expected a level of assurance.");

        AuthnStatement authnStatement = anAuthnStatement().withAuthnContext(
            anAuthnContext().withAuthnContextClassRef(null).build())
            .build();
        Assertion assertion = aSignedAssertion()
            .addAuthnStatement(authnStatement
            ).buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWithUnknownLevelOfAssurance() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Level of assurance 'unknown' is not supported.");

        Assertion assertion = aSignedAssertion()
            .addAuthnStatement(anAuthnStatement()
                .withAuthnContext(anAuthnContext()
                    .withAuthnContextClassRef(anAuthnContextClassRef()
                        .withAuthnContextClasRefValue("unknown")
                        .build())
                    .build())
                .build())
            .buildUnencrypted();

        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2);
    }

    @Test
    public void shouldThrowExceptionWithLowerLevelOfAssuranceThanRequested() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Expected Level of Assurance to be at least LEVEL_2, but was LEVEL_1");

        Assertion assertion = anAssertionWith("some-pid", LEVEL_1_AUTHN_CTX).buildUnencrypted();

        LevelOfAssurance expectedLevelOfAssurance = LevelOfAssurance.LEVEL_2;
        translator.translate(ImmutableList.of(assertion), IN_RESPONSE_TO, expectedLevelOfAssurance);
    }

    private AssertionBuilder aSignedAssertion() {
        return anAssertion()
            .withSubject(aValidSubject().build())
            .withConditions(aValidConditions().build())
            .withSignature(aSignature()
                .withSigningCredential(testRpMsaSigningCredential)
                .build());
    }

    private SubjectBuilder aValidSubject() {
        return aSubject()
            .withSubjectConfirmation(
                aSubjectConfirmation()
                    .withSubjectConfirmationData(aSubjectConfirmationData()
                        .withNotOnOrAfter(DateTime.now().plusMinutes(15))
                        .withInResponseTo(IN_RESPONSE_TO)
                        .build())
                    .build());
    }

    private ConditionsBuilder aValidConditions() {
        return aConditions()
            .withoutDefaultAudienceRestriction()
            .addAudienceRestriction(anAudienceRestriction()
                .withAudienceId(VERIFY_SERVICE_PROVIDER_ENTITY_ID)
                .build());
    }

    private AssertionBuilder anAssertionWith(String pid, String levelOfAssurance) {
        return aSignedAssertion()
            .withSubject(aValidSubject().withPersistentId(pid).build())
            .withConditions(aValidConditions().build())
            .addAuthnStatement(anAuthnStatement()
                .withAuthnContext(anAuthnContext()
                    .withAuthnContextClassRef(anAuthnContextClassRef()
                        .withAuthnContextClasRefValue(levelOfAssurance).build())
                    .build())
                .build());
    }
}