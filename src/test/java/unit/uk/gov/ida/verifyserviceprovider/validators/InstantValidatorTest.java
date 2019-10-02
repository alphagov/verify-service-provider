package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstantValidatorTest {

    private DateTimeComparator dateTimeComparator;

    private InstantValidator validator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        dateTimeComparator = mock(DateTimeComparator.class);

        validator = new InstantValidator(dateTimeComparator);
    }

    @Test
    public void shouldValidateInstantIsInExpectedRange() {
        DateTime instant = new DateTime().minusMinutes(1);

        validator.validate(instant, "any-instant-name");
    }

    @Test
    public void shouldThrowExceptionIfInstantOldenThanFiveMinutes() {
        DateTime instant = new DateTime().minusMinutes(6);
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("some-instant-name is too far in the past ");

        validator.validate(instant, "some-instant-name");
    }

    @Test
    public void shouldThrowExceptionWhenInstantIsInTheFuture() {
        DateTime instant = new DateTime().plusMinutes(1);
        String errorMessage = String.format("%s is in the future %s",
            "some-instant-name",
            instant.withZone(UTC).toString(dateHourMinuteSecond()));

        when(dateTimeComparator.isAfterNow(instant)).thenReturn(true);

        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage(errorMessage);

        validator.validate(instant, "some-instant-name");
    }
}