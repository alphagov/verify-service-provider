package unit.uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.saml.core.transformers.VerifyMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.core.validation.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.hub.factories.UserIdHashFactory;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TestTranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier;
import uk.gov.ida.verifyserviceprovider.services.VerifyAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.ConditionsBuilder.aConditions;
import static uk.gov.ida.saml.core.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class VerifyAssertionTranslatorTest {

    private VerifyAssertionTranslator verifyAssertionService;

    @Mock
    private SubjectValidator subjectValidator;

    @Mock
    private SamlAssertionsSignatureValidator hubSignatureValidator;

    @Mock
    private AssertionAttributeStatementValidator attributeStatementValidator;

    @Mock
    private VerifyMatchingDatasetUnmarshaller verifyMatchingDatasetUnmarshaller;

    @Mock
    private LevelOfAssuranceValidator levelOfAssuranceValidator;

    @Mock
    private UserIdHashFactory userIdHashFactory;

    @Mock
    private MatchingDatasetToNonMatchingAttributesMapper matchingDatasetToNonMatchingAttributesMapper;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        initMocks(this);

        verifyAssertionService = new VerifyAssertionTranslator(
                hubSignatureValidator,
                subjectValidator,
                attributeStatementValidator,
                verifyMatchingDatasetUnmarshaller,
                new AssertionClassifier(),
                matchingDatasetToNonMatchingAttributesMapper,
                levelOfAssuranceValidator,
                userIdHashFactory);
        doNothing().when(subjectValidator).validate(any(), any());
        when(hubSignatureValidator.validate(any(), any())).thenReturn(mock(ValidatedAssertions.class));

        DateTimeFreezer.freezeTime();
    }

    @After
    public void tearDown() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void shouldThrowExceptionIfIssueInstantMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssueInstant(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion IssueInstant is missing.");
        verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Id is missing or blank.");
        verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsBlankWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID("");

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Id is missing or blank.");
        verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId(null).build());

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueIsBlankWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId("").build());

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfMissingAssertionVersionWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion has missing Version.");
        verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }


    @Test
    public void shouldThrowExceptionIfAssertionVersionInvalidWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(SAMLVersion.VERSION_10);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion declared an illegal Version attribute value.");
        verifyAssertionService.validateIdpAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldNotThrowExceptionsWhenAssertionsAreValid() {
        Assertion authnAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted();
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();

        verifyAssertionService.validate(authnAssertion, mdsAssertion, "requestId", LevelOfAssurance.LEVEL_1, LEVEL_2);

        verify(subjectValidator, times(2)).validate(any(), any());
        verify(hubSignatureValidator, times(2)).validate(any(), any());
        verify(levelOfAssuranceValidator, times(1)).validate(LEVEL_2, LEVEL_1);
    }

    @Test
    public void shouldCorrectlyExtractLevelOfAssurance() {
        Assertion authnAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted();

        LevelOfAssurance loa = verifyAssertionService.extractLevelOfAssuranceFrom(authnAssertion);

        assertThat(loa).isEqualTo(LevelOfAssurance.LEVEL_2);
    }


    @Test
    public void shouldThrowExceptionWhenLevelOfAssuranceNotPresent() {
        Assertion authnAssertion = anAuthnStatementAssertion(null, "requestId").buildUnencrypted();
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Expected a level of assurance.");
        verifyAssertionService.translateSuccessResponse(ImmutableList.of(authnAssertion, mdsAssertion), "requestId", LEVEL_2, "default-entity-id");
    }

    @Test
    public void shouldThrowExceptionWithUnknownLevelOfAssurance() throws Exception {
        Assertion authnAssertion = anAuthnStatementAssertion("unknown", "requestId").buildUnencrypted();
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Level of assurance 'unknown' is not supported.");
        verifyAssertionService.translateSuccessResponse(ImmutableList.of(authnAssertion, mdsAssertion), "requestId", LEVEL_2, "default-entity-id");
    }

    @Test
    public void expectedHashContainedInResponseBodyWhenUserIdFactoryIsCalledOnce() {

        String requestId = "requestId";
        String expectedHashed = "a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298";
        TestTranslatedNonMatchingResponseBody expectedNonMatchingResponseBody = new TestTranslatedNonMatchingResponseBody(NonMatchingScenario.IDENTITY_VERIFIED, expectedHashed, LEVEL_2, null);

        Assertion authnAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, requestId).buildUnencrypted();
        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), requestId).buildUnencrypted();

        final String nameId = authnAssertion.getSubject().getNameID().getValue();
        final String issuerId = authnAssertion.getIssuer().getValue();

        when(userIdHashFactory.hashId(eq(issuerId), eq(nameId), eq(Optional.of(AuthnContext.LEVEL_2))))
                .thenReturn(expectedHashed);

        TranslatedNonMatchingResponseBody responseBody = verifyAssertionService.translateSuccessResponse(ImmutableList.of(authnAssertion, mdsAssertion), "requestId", LEVEL_2, "default-entity-id");

        verify(userIdHashFactory, times(1)).hashId(issuerId, nameId, Optional.of(AuthnContext.LEVEL_2));
        assertThat(responseBody.toString()).contains(expectedNonMatchingResponseBody.getPid());
    }

    private static AssertionBuilder aMatchingDatasetAssertionWithSignature(List<Attribute> attributes, Signature signature, String requestId) {
        return anAssertion()
                .withId("mds-assertion")
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .withSubject(anAssertionSubject(requestId))
                .withSignature(signature)
                .addAttributeStatement(anAttributeStatement().addAllAttributes(attributes).build())
                .withConditions(aConditions().build());
    }

    private static AssertionBuilder anAuthnStatementAssertion(String authnContext, String inResponseTo) {
        return anAssertion()
                .addAuthnStatement(
                        anAuthnStatement()
                                .withAuthnContext(
                                        anAuthnContext()
                                                .withAuthnContextClassRef(
                                                        anAuthnContextClassRef()
                                                                .withAuthnContextClasRefValue(authnContext)
                                                                .build())
                                                .build())
                                .build())
                .withSubject(
                        aSubject()
                                .withSubjectConfirmation(
                                        aSubjectConfirmation()
                                                .withSubjectConfirmationData(
                                                        aSubjectConfirmationData()
                                                                .withInResponseTo(inResponseTo)
                                                                .build()
                                                ).build()
                                ).build())
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build());
    }

    private static Subject anAssertionSubject(final String inResponseTo) {
        return aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withNotOnOrAfter(DateTime.now())
                                                .withInResponseTo(inResponseTo)
                                                .build()
                                ).build()
                ).build();
    }

    private static Signature anIdpSignature() {
        return aSignature().withSigningCredential(
                new TestCredentialFactory(STUB_IDP_PUBLIC_PRIMARY_CERT, STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
                        .getSigningCredential()).build();

    }
}
