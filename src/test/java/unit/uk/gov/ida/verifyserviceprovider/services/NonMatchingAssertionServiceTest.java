package unit.uk.gov.ida.verifyserviceprovider.services;

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

import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.transformers.VerifyMatchingDatasetUnmarshaller;

import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.services.NonMatchingAssertionService;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.aCycle3DatasetAssertion;
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

public class NonMatchingAssertionServiceTest {

    private NonMatchingAssertionService nonMatchingAssertionService;
    @Mock
     private AssertionValidator assertionValidator;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private SubjectValidator subjectValidator;

    @Mock
    private ConditionsValidator conditionsValidator;

    @Mock
    private SamlAssertionsSignatureValidator hubSignatureValidator;


    @Mock
    private VerifyMatchingDatasetUnmarshaller verifyMatchingDatasetUnmarshaller;


    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        initMocks(this);

        assertionValidator = new AssertionValidator(instantValidator,subjectValidator,conditionsValidator);

        nonMatchingAssertionService = new NonMatchingAssertionService(hubSignatureValidator,
                assertionValidator
        );
        doNothing().when(assertionValidator.getInstantValidator()).validate(any(), any());
        doNothing().when(assertionValidator.getSubjectValidator()).validate(any(), any());
        doNothing().when(assertionValidator.getConditionsValidator()).validate(any(), any());
        when(hubSignatureValidator.validate(any(), any())).thenReturn(mock(ValidatedAssertions.class));

        DateTimeFreezer.freezeTime();
    }

    @After
    public void tearDown() {
        DateTimeFreezer.unfreezeTime();
    }

    @Test
    public void shouldThrowExceptionIfIssueInstantMissingWhenValidatingIdPAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssueInstant(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion IssueInstant is missing.");
        nonMatchingAssertionService.validateIdPAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsMissingWhenValidatingIdPAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Id is missing or blank.");
        nonMatchingAssertionService.validateIdPAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsBlankWhenValidatingIdPAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID("");

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion Id is missing or blank.");
        nonMatchingAssertionService.validateIdPAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerMissingWhenValidatingIdPAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        nonMatchingAssertionService.validateIdPAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueMissingWhenValidatingIdPAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId(null).build());

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        nonMatchingAssertionService.validateIdPAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueIsBlankWhenValidatingIdPAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId("").build());

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        nonMatchingAssertionService.validateIdPAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldThrowExceptionIfMissingAssertionVersionWhenValidatingIdPAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(null);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion has missing Version.");
        nonMatchingAssertionService.validateIdPAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }


    @Test
    public void shouldThrowExceptionIfAssertionVersionInvalidWhenValidatingIdPAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(SAMLVersion.VERSION_10);

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage("Assertion with id mds-assertion declared an illegal Version attribute value.");
        nonMatchingAssertionService.validateIdPAssertion(assertion, "not-used", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldNotThrowExceptionsWhenAssertionsAreValid() {
        List<Assertion> assertions = asList(
                aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted(),
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted());

        nonMatchingAssertionService.validate(assertions,"requestId", LevelOfAssurance.LEVEL_1);

        verify(assertionValidator.getSubjectValidator(), times(2)).validate(any(), any());
        verify(hubSignatureValidator, times(2)).validate(any(), any());
    }



    public static AssertionBuilder aMatchingDatasetAssertionWithSignature(List<Attribute> attributes, Signature signature, String requestId) {
        return anAssertion()
                .withId("mds-assertion")
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .withSubject(anAssertionSubject(requestId))
                .withSignature(signature)
                .addAttributeStatement(anAttributeStatement().addAllAttributes(attributes).build())
                .withConditions(aConditions().build());
    }

    public static AssertionBuilder anAuthnStatementAssertion(String authnContext, String inResponseTo) {
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

    public static Subject anAssertionSubject(final String inResponseTo) {
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

    public static Signature anIdpSignature() {
        return aSignature().withSigningCredential(
                new TestCredentialFactory(STUB_IDP_PUBLIC_PRIMARY_CERT, STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY)
                        .getSigningCredential()).build();

    }

}