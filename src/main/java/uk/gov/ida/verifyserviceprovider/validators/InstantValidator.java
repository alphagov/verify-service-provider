package uk.gov.ida.verifyserviceprovider.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;

public class InstantValidator {

    private static final Duration MAXIMUM_INSTANT_AGE = Duration.standardMinutes(5);
    private final String instantName;
    private final DateTimeComparator dateTimeComparator;

    public InstantValidator(String instantName, DateTimeComparator dateTimeComparator) {
        this.instantName = instantName;
        this.dateTimeComparator = dateTimeComparator;
    }

    public void validate(DateTime instant) {
        Duration age = new Duration(instant, DateTime.now());
        if (age.isLongerThan(MAXIMUM_INSTANT_AGE)) {
            throw new SamlResponseValidationException(String.format("%s is too far in the past %s",
                    instantName,
                    PeriodFormat.getDefault().print(age.toPeriod()))
            );
        }

        if (!dateTimeComparator.isBeforeNowFuzzy(instant)) {
            throw new SamlResponseValidationException(String.format("%s is in the future %s",
                    instantName,
                    instant.withZone(UTC).toString(dateHourMinuteSecond()))
            );
        }
    }
}
