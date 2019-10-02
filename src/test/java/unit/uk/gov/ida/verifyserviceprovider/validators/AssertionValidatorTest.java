package unit.uk.gov.ida.verifyserviceprovider.validators;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;

public class AssertionValidatorTest {

    private AssertionValidator validator;

    private InstantValidator instantValidator;
    private SubjectValidator subjectValidator;
    private ConditionsValidator conditionsValidator;
    private Assertion assertion;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        instantValidator = mock(InstantValidator.class);
        subjectValidator = mock(SubjectValidator.class);
        conditionsValidator = mock(ConditionsValidator.class);
        assertion = mock(Assertion.class);
        AuthnStatement authnStatement = mock(AuthnStatement.class);

        validator = new AssertionValidator(
            instantValidator,
            subjectValidator,
            conditionsValidator
        );

        when(assertion.getAuthnStatements()).thenReturn(ImmutableList.of(authnStatement));

        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldValidateAssertionIssueInstant() {
        DateTime issueInstant = new DateTime();
        when(assertion.getIssueInstant()).thenReturn(issueInstant);

        validator.validate(assertion, "any-expected-in-response-to", "any-entity-id");

        verify(instantValidator).validate(issueInstant, "Assertion IssueInstant");
    }

    @Test
    public void shouldValidateAssertionSubject() {
        Subject subject = mock(Subject.class, Answers.RETURNS_DEEP_STUBS);
        when(assertion.getSubject()).thenReturn(subject);
        when(subject.getNameID().getValue()).thenReturn("any-value");

        validator.validate(assertion, "some-expected-in-response-to", "any-entity-id");

        verify(subjectValidator).validate(subject, "some-expected-in-response-to");
    }

    @Test
    public void shouldValidateAssertionConditions() {
        Conditions conditions = mock(Conditions.class);
        when(assertion.getConditions()).thenReturn(conditions);

        validator.validate(assertion, "any-expected-in-response-to", "some-entity-id");

        verify(conditionsValidator).validate(conditions, "some-entity-id");
    }

    @Test
    public void shouldThrowExceptionIfAuthnStatementsIsNull() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one authn statement is expected.");

        when(assertion.getAuthnStatements()).thenReturn(null);

        validator.validate(assertion, "some-expected-in-response-to", "any-entity-id");
    }

    @Test
    public void shouldThrowExceptionIfAuthnStatementsIsEmpty() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one authn statement is expected.");

        when(assertion.getAuthnStatements()).thenReturn(Collections.emptyList());

        validator.validate(assertion, "some-expected-in-response-to", "any-entity-id");
    }

    @Test
    public void shouldThrowExceptionIfMoreThanOneAuthnStatements() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one authn statement is expected.");

        when(assertion.getAuthnStatements()).thenReturn(ImmutableList.of(
            anAuthnStatement().build(),
            anAuthnStatement().build()
        ));

        validator.validate(assertion, "some-expected-in-response-to", "any-entity-id");
    }

    @Test
    public void shouldValidateAssertionAuthnInstant() {
        DateTime issueInstant = new DateTime();
        when(assertion.getAuthnStatements().get(0).getAuthnInstant()).thenReturn(issueInstant);

        validator.validate(assertion, "any-expected-in-response-to", "any-entity-id");

        verify(instantValidator).validate(issueInstant, "Assertion AuthnInstant");
    }
}