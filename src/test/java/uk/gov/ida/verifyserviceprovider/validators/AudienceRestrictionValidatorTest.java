package uk.gov.ida.verifyserviceprovider.validators;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.AudienceRestrictionBuilder.anAudienceRestriction;

public class AudienceRestrictionValidatorTest {

    private AudienceRestrictionValidator validator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        validator = new AudienceRestrictionValidator();

        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldNotComplainWhenCorrectDataIsPassed() {
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI("some-entity-id");

        AudienceRestriction audienceRestriction = mock(AudienceRestriction.class, Answers.RETURNS_DEEP_STUBS);
        when(audienceRestriction.getAudiences()).thenReturn(ImmutableList.of(audience));

        validator.validate(ImmutableList.of(audienceRestriction), "some-entity-id");
    }

    @Test
    public void shouldThrowExceptionWhenAudienceRestrictionsIsNull() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one audience restriction is expected.");

        List<AudienceRestriction> audienceRestrictions = null;
        validator.validate(audienceRestrictions, "any-entity-id");
    }

    @Test
    public void shouldThrowExceptionWhenAudienceRestrictionsHasMoreThanOneElements() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one audience restriction is expected.");

        List<AudienceRestriction> audienceRestrictions = ImmutableList.of(
            anAudienceRestriction().build(),
            anAudienceRestriction().build()
        );

        validator.validate(audienceRestrictions, "any-entity-id");
    }

    @Test
    public void shouldThrowExceptionWhenAudiencesIsNull() {
        AudienceRestriction audienceRestriction = mock(AudienceRestriction.class, Answers.RETURNS_DEEP_STUBS);
        when(audienceRestriction.getAudiences()).thenReturn(null);

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one audience is expected.");

        validator.validate(ImmutableList.of(audienceRestriction), "any-entity-id");
    }

    @Test
    public void shouldThrowExceptionWhenAudiencesIsMoreThanOne() {
        AudienceRestriction audienceRestriction = anAudienceRestriction().build();
        audienceRestriction.getAudiences().add(new AudienceBuilder().buildObject());
        audienceRestriction.getAudiences().add(new AudienceBuilder().buildObject());

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Exactly one audience is expected.");

        validator.validate(ImmutableList.of(audienceRestriction), "any-entity-id");
    }

    @Test
    public void shouldThrowExceptionWhenAudienceUriDoesNotMatchTheEntityId() {
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI("some-entity-id");

        AudienceRestriction audienceRestriction = mock(AudienceRestriction.class, Answers.RETURNS_DEEP_STUBS);
        when(audienceRestriction.getAudiences()).thenReturn(ImmutableList.of(audience));

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage(String.format("Audience must match entity ID. Expected %s but was %s", "unknown-entity-id", "some-entity-id"));

        validator.validate(ImmutableList.of(audienceRestriction), "unknown-entity-id");
    }
}