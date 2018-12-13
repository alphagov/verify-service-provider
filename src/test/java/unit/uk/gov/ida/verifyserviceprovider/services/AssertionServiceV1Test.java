package unit.uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.impl.CollectionCredentialResolver;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.domain.SamlStatusCode;
import uk.gov.ida.saml.core.test.PrivateKeyStoreFactory;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.ConditionsBuilder;
import uk.gov.ida.saml.core.test.builders.IssuerBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectBuilder;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.verifyserviceprovider.dto.Scenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.ResponseFactory;
import uk.gov.ida.verifyserviceprovider.services.AssertionServiceV1;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.ida.saml.core.test.builders.StatusCodeBuilder.aStatusCode;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.Scenario.SUCCESS_MATCH;

public class AssertionServiceV1Test {

    private static final String IN_RESPONSE_TO = "_some-request-id";
    private static final String VERIFY_SERVICE_PROVIDER_ENTITY_ID = "default-entity-id";
    private AssertionServiceV1 assertionServiceV1;
    private Credential testRpMsaSigningCredential = createMSSigningCredential();

    private Credential createMSSigningCredential() {
        Credential signingCredential = new TestCredentialFactory(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY).getSigningCredential();
        ((BasicCredential) signingCredential).setEntityId(TestEntityIds.TEST_RP_MS);
        return signingCredential;
    }

    @Before
    public void setUp() throws Exception {
        PrivateKey privateKey = new PrivateKeyStoreFactory().create(TestEntityIds.TEST_RP).getEncryptionPrivateKeys().get(0);
        KeyPair keyPair = new KeyPair(KeySupport.derivePublicKey(privateKey), privateKey);
        List<KeyPair> keyPairs = asList(keyPair, keyPair);
        ResponseFactory responseFactory = new ResponseFactory(keyPairs);

        CollectionCredentialResolver resolver = new CollectionCredentialResolver(asList(testRpMsaSigningCredential));
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(resolver, DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());

        DateTimeComparator dateTimeComparator = new DateTimeComparator(Duration.standardSeconds(5));

        assertionServiceV1 = responseFactory.createAssertionServiceV1(explicitKeySignatureTrustEngine, dateTimeComparator);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldTranslateValidAssertion() {
        TranslatedResponseBody result = assertionServiceV1.translateSuccessResponse(ImmutableList.of(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).buildUnencrypted()
        ), IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID);
        assertThat(result).isEqualTo(new TranslatedResponseBody(
            SUCCESS_MATCH,
            "some-pid",
            LEVEL_2,
            null
        ));
    }

    @Test
    public void shouldAllowHigherLevelOfAssuranceThanRequested() throws Exception {
        TranslatedResponseBody result = assertionServiceV1.translateSuccessResponse(ImmutableList.of(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).buildUnencrypted()
        ), IN_RESPONSE_TO, LEVEL_1, VERIFY_SERVICE_PROVIDER_ENTITY_ID);
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

        assertionServiceV1.translateSuccessResponse(emptyList(), IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID);
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsIsNull() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one assertion is expected.");

        assertionServiceV1.translateSuccessResponse(null, IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID);
    }

    @Test
    public void shouldThrowExceptionWhenAssertionsListSizeIsLargerThanOne() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one assertion is expected.");

        assertionServiceV1.translateSuccessResponse(
            ImmutableList.of(
                anAssertion().buildUnencrypted(),
                anAssertion().buildUnencrypted()
            ),
            IN_RESPONSE_TO,
            LEVEL_2,
            VERIFY_SERVICE_PROVIDER_ENTITY_ID);
    }

    @Test
    public void shouldThrowExceptionWhenAssertionIsNotSigned() throws Exception {
        expectedException.expect(SamlTransformationErrorException.class);
        expectedException.expectMessage("SAML Validation Specification: Message signature is not signed");

        assertionServiceV1.translateSuccessResponse(Collections.singletonList(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX).withoutSigning().buildUnencrypted()),
            IN_RESPONSE_TO,
            LEVEL_2,
            VERIFY_SERVICE_PROVIDER_ENTITY_ID
        );
    }

    @Test
    public void shouldThrowExceptionWhenAssertionSignedByUnknownKey() throws Exception {
        expectedException.expect(SamlTransformationErrorException.class);
        expectedException.expectMessage("SAML Validation Specification: Signature was not valid.");

        Credential unknownSigningCredential = new TestCredentialFactory(TEST_PUBLIC_CERT, TEST_PRIVATE_KEY).getSigningCredential();
        assertionServiceV1.translateSuccessResponse(Collections.singletonList(
            anAssertionWith("some-pid", LEVEL_2_AUTHN_CTX)
                .withSignature(aSignature().withSigningCredential(unknownSigningCredential).build())
                .buildUnencrypted()),
            IN_RESPONSE_TO,
            LEVEL_2,
            VERIFY_SERVICE_PROVIDER_ENTITY_ID
        );
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

        assertionServiceV1.translateSuccessResponse(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID);
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

        assertionServiceV1.translateSuccessResponse(ImmutableList.of(assertion), IN_RESPONSE_TO, LEVEL_2, VERIFY_SERVICE_PROVIDER_ENTITY_ID);
    }

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithNoSubStatusCode() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Missing status code for non-Success response");

        StatusCode statusCode = aStatusCode().withValue(StatusCode.RESPONDER).build();
        assertionServiceV1.translateNonSuccessResponse(statusCode);
    }

    @Test
    public void shouldReturnScenarioCancelledWhenNoAuthnContextStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AUTHN_CONTEXT).build())
                .build();
        TranslatedResponseBody response = assertionServiceV1.translateNonSuccessResponse(statusCode);
        assertThat(response.getScenario()).isEqualTo(Scenario.CANCELLATION);
    }

    @Test
    public void shouldReturnScenarioNoMatchWhenNoMatchStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(SamlStatusCode.NO_MATCH).build())
                .build();
        TranslatedResponseBody response = assertionServiceV1.translateNonSuccessResponse(statusCode);
        assertThat(response.getScenario()).isEqualTo(Scenario.NO_MATCH);
    }

    @Test
    public void shouldReturnScenarioAuthenticationFailedWhenAuthnFailedStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.AUTHN_FAILED).build())
                .build();
        TranslatedResponseBody response = assertionServiceV1.translateNonSuccessResponse(statusCode);
        assertThat(response.getScenario()).isEqualTo(Scenario.AUTHENTICATION_FAILED);
    }

    @Test
    public void shouldReturnScenarioRequestErrorWhenRequesterStatus() {
        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.REQUESTER).build())
                .build();
        TranslatedResponseBody response = assertionServiceV1.translateNonSuccessResponse(statusCode);
        assertThat(response.getScenario()).isEqualTo(Scenario.REQUEST_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithUnrecognisedStatus() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Unknown SAML sub-status: urn:oasis:names:tc:SAML:2.0:status:NoAvailableIDP");

        StatusCode statusCode = aStatusCode()
                .withValue(StatusCode.RESPONDER)
                .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AVAILABLE_IDP).build())
                .build();
        assertionServiceV1.translateNonSuccessResponse(statusCode);
    }

    private AssertionBuilder aSignedAssertion() {
        Issuer issuer = IssuerBuilder.anIssuer().build();
        issuer.setValue(TestEntityIds.TEST_RP_MS);
        return anAssertion()
                .withIssuer(issuer)
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