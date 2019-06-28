package uk.gov.ida.verifyserviceprovider.validators;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.ConditionsBuilder.aConditions;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class VerifyAssertionValidatorTest {
    private VerifyAssertionValidator validator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private Assertion assertion;
    private SamlAssertionsSignatureValidator signatureValidator;
    private AssertionAttributeStatementValidator attributeValidator;
    private SubjectValidator subjectValidator;

    @BeforeClass
    public static void beforeAll() {
        IdaSamlBootstrap.bootstrap();
    }

    @Before
    public void setUp() {
        signatureValidator = mock(SamlAssertionsSignatureValidator.class);
        attributeValidator = mock(AssertionAttributeStatementValidator.class);
        subjectValidator = mock(SubjectValidator.class);

        assertion = mock(Assertion.class);

        validator = new VerifyAssertionValidator(
            signatureValidator,
            attributeValidator,
            subjectValidator
        );

    }

//    @Test
//    public void shouldValidateAssertionIssueInstant() {
//        assertion = mock(Assertion.class);
//        DateTime issueInstant = new DateTime();
//        when(assertion.getIssueInstant()).thenReturn(issueInstant);
//
//        validator.validate(asList<assertion>, "any-expected-in-response-to", "any-entity-id");
//
//        verify(instantValidator).validate(issueInstant, "Assertion IssueInstant");
//    }

    public static AssertionBuilder aMatchingDatasetAssertionWithSignature(List<Attribute> attributes, Signature signature, String requestId) {
        return anAssertion()
            .withId("mds-assertion")
            .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
            .withSubject(anAssertionSubject(requestId))
            .withSignature(signature)
            .addAttributeStatement(anAttributeStatement().addAllAttributes(attributes).build())
            .withConditions(aConditions().build());
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

    @Test
    public void shouldValidateAssertionSubjects() {
        Subject subject = mock(Subject.class, Answers.RETURNS_DEEP_STUBS);
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setSubject(subject);

        validator.validate(assertion, "some-expected-in-response-to");

        verify(subjectValidator).validate(subject, "some-expected-in-response-to");
    }

    @Test
    public void shouldThrowExceptionIfIssueInstantMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssueInstant(null);

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion IssueInstant is missing.");
        validator.validate(assertion, "not-used");
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID(null);

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion Id is missing or blank.");
        validator.validate(assertion, "not-used");
    }

    @Test
    public void shouldThrowExceptionIfAssertionIdIsBlankWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setID("");

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion Id is missing or blank.");
        validator.validate(assertion, "not-used");
    }

    @Test
    public void shouldThrowExceptionIfIssuerMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(null);

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        validator.validate(assertion, "not-used");
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueMissingWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId(null).build());

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        validator.validate(assertion, "not-used");
    }

    @Test
    public void shouldThrowExceptionIfIssuerValueIsBlankWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setIssuer(anIssuer().withIssuerId("").build());

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
        validator.validate(assertion, "not-used");
    }

    @Test
    public void shouldThrowExceptionIfMissingAssertionVersionWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(null);

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion with id mds-assertion has missing Version.");
        validator.validate(assertion, "not-used");
    }

    @Test
    public void shouldThrowExceptionIfAssertionVersionInvalidWhenValidatingIdpAssertion() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
        assertion.setVersion(SAMLVersion.VERSION_10);

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Assertion with id mds-assertion declared an illegal Version attribute value.");
        validator.validate(assertion, "not-used");
    }

    @Test
    public void shouldNotThrowExceptionsWhenAssertionsAreValid() {
        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();

        validator.validate(assertion,"requestId");

        verify(subjectValidator, times(1)).validate(eq(assertion.getSubject()), eq("requestId"));
        verify(signatureValidator, times(1)).validate(any(), any());
        verify(attributeValidator, times(1)).validate(eq(assertion));
    }
}