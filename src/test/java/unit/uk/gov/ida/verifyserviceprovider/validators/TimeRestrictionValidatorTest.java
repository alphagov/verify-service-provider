package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import uk.gov.ida.verifyserviceprovider.validators.TimeRestrictionValidator;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeRestrictionValidatorTest {

    private DateTimeComparator dateTimeComparator;

    private TimeRestrictionValidator validator;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        dateTimeComparator = mock(DateTimeComparator.class);

        validator = new TimeRestrictionValidator(dateTimeComparator);
    }

    @Test
    public void validateNotOnOrAfterShouldThrowExceptionWhenNotOnOrAfterIsBeforeNow() {
        DateTime notOnOrAfter = new DateTime();
        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage(String.format(
            "Assertion is not valid on or after %s",
            notOnOrAfter.withZone(UTC).toString(dateHourMinuteSecond())
        ));

        when(dateTimeComparator.isBeforeNow(notOnOrAfter)).thenReturn(true);

        validator.validateNotOnOrAfter(notOnOrAfter);
    }

    @Test
    public void validateNotBeforeShouldThrowExceptionWhenNotBeforeIsAfterNow() {
        DateTime notBefore = new DateTime();
        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage(String.format(
            "Assertion is not valid before %s",
            notBefore.withZone(UTC).toString(dateHourMinuteSecond())
        ));

        when(dateTimeComparator.isAfterNow(notBefore)).thenReturn(true);

        validator.validateNotBefore(notBefore);
    }
}