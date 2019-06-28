package uk.gov.ida.verifyserviceprovider.validators;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyAssertionsValidatorTest {

    private VerifyAssertionsValidator validator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private VerifyAssertionValidator verifyAssertionValidator;
    private Issuer expectedIssuer;
    private Subject subject;

    @Before
    public void setUp() {
        expectedIssuer = mock(Issuer.class, Answers.RETURNS_DEEP_STUBS);
        when(expectedIssuer.getValue()).thenReturn("any-value");

        subject = mock(Subject.class, Answers.RETURNS_DEEP_STUBS);
        when(subject.getNameID().getValue()).thenReturn("any-value");
        verifyAssertionValidator = mock(VerifyAssertionValidator.class);
        validator = new VerifyAssertionsValidator(
            verifyAssertionValidator
        );
    }

//    @Test
//    public void shouldValidateAssertionIssueInstant() {
//        assertion = verifyAssertionValidator(Assertion.class);
//        DateTime issueInstant = new DateTime();
//        when(assertion.getIssueInstant()).thenReturn(issueInstant);
//
//        validator.validate(asList<assertion>, "any-expected-in-response-to", "any-entity-id");
//
//        verify(instantValidator).validate(issueInstant, "Assertion IssueInstant");
//    }

    @Test
    public void shouldValidateThatThereAreTwoAssertions() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Two assertions are expected from a Verify IDP.");
        validator.validate(emptyList(), "some-expected-in-response-to");
    }

    @Test
    public void shouldValidateAssertionIssuersMatch() {
        Assertion leftAssertion = mock(Assertion.class);
        Assertion rightAssertion = mock(Assertion.class);

        Issuer anotherIssuer = mock(Issuer.class, Answers.RETURNS_DEEP_STUBS);
        when(anotherIssuer.getValue()).thenReturn("another-value");

        when(leftAssertion.getIssuer()).thenReturn(expectedIssuer);
        when(rightAssertion.getIssuer()).thenReturn(anotherIssuer);

        Assertions.assertThatThrownBy(
            () -> validator.validate(asList(leftAssertion, rightAssertion), "some-expected-in-response-to")
        ).isInstanceOf(SamlResponseValidationException.class)
            .hasMessage("IDP matching dataset and authn statement assertions do not contain matching issuers");
    }

    @Test
    public void shouldValidateAssertionSubjectsMatch() {
        Subject subjectTwo = mock(Subject.class, Answers.RETURNS_DEEP_STUBS);

        Assertion leftAssertion = mock(Assertion.class);
        Assertion rightAssertion = mock(Assertion.class);

        when(leftAssertion.getIssuer()).thenReturn(expectedIssuer);
        when(rightAssertion.getIssuer()).thenReturn(expectedIssuer);
        when(leftAssertion.getSubject()).thenReturn(subject);
        when(rightAssertion.getSubject()).thenReturn(subjectTwo);
        when(subject.getNameID().getValue()).thenReturn("another-value");
        Assertions.assertThatThrownBy(
            () -> validator.validate(asList(leftAssertion, rightAssertion), "some-expected-in-response-to")
        ).isInstanceOf(SamlResponseValidationException.class)
                    .hasMessage("IDP matching dataset and authn statement assertions do not contain matching persistent identifiers");
    }
//
//    @Test
//    public void shouldThrowExceptionIfIssueInstantMissingWhenValidatingIdpAssertion() {
//        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
//        assertion.setIssueInstant(null);
//
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Assertion IssueInstant is missing.");
//        validator.verifyAssertionValidator.validate(assertion, "not-used");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfAssertionIdIsMissingWhenValidatingIdpAssertion() {
//        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
//        assertion.setID(null);
//
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Assertion Id is missing or blank.");
//        validator.verifyAssertionValidator.validate(assertion, "not-used");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfAssertionIdIsBlankWhenValidatingIdpAssertion() {
//        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
//        assertion.setID("");
//
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Assertion Id is missing or blank.");
//        validator.verifyAssertionValidator.validate(assertion, "not-used");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfIssuerMissingWhenValidatingIdpAssertion() {
//        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
//        assertion.setIssuer(null);
//
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
//        validator.verifyAssertionValidator.validate(assertion, "not-used");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfIssuerValueMissingWhenValidatingIdpAssertion() {
//        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
//        assertion.setIssuer(anIssuer().withIssuerId(null).build());
//
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
//        validator.verifyAssertionValidator.validate(assertion, "not-used");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfIssuerValueIsBlankWhenValidatingIdpAssertion() {
//        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
//        assertion.setIssuer(anIssuer().withIssuerId("").build());
//
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Assertion with id mds-assertion has missing or blank Issuer.");
//        validator.verifyAssertionValidator.validate(assertion, "not-used");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfMissingAssertionVersionWhenValidatingIdpAssertion() {
//        Assertion assertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
//        assertion.setVersion(null);
//
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Assertion with id mds-assertion has missing Version.");
//        validator.verifyAssertionValidator.validate(assertion, "not-used");
//    }
//
//
//    @Test
//    public void shouldThrowExceptionIfAssertionVersionInvalidWhenValidatingIdpAssertion() {
//        Assertion assertion = verifyAssertionValidator(Assertion.class);
//        assertion.setVersion(SAMLVersion.VERSION_10);
//
////        expectedException.expect(SamlResponseValidationException.class);
////        expectedException.expectMessage("Assertion with id mds-assertion declared an illegal Version attribute value.");
//        validator.verifyAssertionValidator.validate(assertion, "not-used");
//    }
//
//    @Test
//    public void shouldNotThrowExceptionsWhenAssertionsAreValid() {
//        Assertion authnAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted();
//        Assertion mdsAssertion = aMatchingDatasetAssertionWithSignature(emptyList(), anIdpSignature(), "requestId").buildUnencrypted();
//
//        validator.validate(asList(authnAssertion, mdsAssertion),"requestId");
//
//        verify(subjectValidator, times(2)).validate(any(), any());
//        verify(signatureValidator, times(2)).validate(any(), any());
//    }
//
//    @Test
//    public void shouldValidateAssertionConditions() {
//        Conditions conditions = verifyAssertionValidator(Conditions.class);
//        when(assertion.getConditions()).thenReturn(conditions);
//
//        validator.validate(assertion, "any-expected-in-response-to", "some-entity-id");
//
//        verify(conditionsValidator).validate(conditions, "some-entity-id");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfAuthnStatementsIsNull() {
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Exactly one authn statement is expected.");
//
//        when(assertion.getAuthnStatements()).thenReturn(null);
//
//        validator.validate(assertion, "some-expected-in-response-to", "any-entity-id");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfAuthnStatementsIsEmpty() {
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Exactly one authn statement is expected.");
//
//        when(assertion.getAuthnStatements()).thenReturn(Collections.emptyList());
//
//        validator.validate(assertion, "some-expected-in-response-to", "any-entity-id");
//    }
//
//    @Test
//    public void shouldThrowExceptionIfMoreThanOneAuthnStatements() {
//        expectedException.expect(SamlResponseValidationException.class);
//        expectedException.expectMessage("Exactly one authn statement is expected.");
//
//        when(assertion.getAuthnStatements()).thenReturn(ImmutableList.of(
//            anAuthnStatement().build(),
//            anAuthnStatement().build()
//        ));
//
//
//    }
//
//    @Test
//    public void shouldValidateAssertionAuthnInstant() {
//        DateTime issueInstant = new DateTime();
//        when(assertion.getAuthnStatements().get(0).getAuthnInstant()).thenReturn(issueInstant);
//
//        validator.validate(assertion, "any-expected-in-response-to", "any-entity-id");
//
//        verify(instantValidator).validate(issueInstant, "Assertion AuthnInstant");
//    }
}
