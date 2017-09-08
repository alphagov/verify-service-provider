package uk.gov.ida.verifyserviceprovider.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;

public class IssueInstantValidator {

    private static final Duration ISSUE_INSTANT_DURATION = Duration.standardMinutes(5);
    private final String issueInstantName;
    private final DateTimeComparator dateTimeComparator;

    public IssueInstantValidator(String issueInstantName, DateTimeComparator dateTimeComparator) {
        this.issueInstantName = issueInstantName;
        this.dateTimeComparator = dateTimeComparator;
    }

    public void validate(DateTime issueInstant) {
        Duration timeSinceCreation = new Duration(issueInstant, DateTime.now());
        if (timeSinceCreation.isLongerThan(ISSUE_INSTANT_DURATION)) {
            throw new SamlResponseValidationException(String.format("%s IssueInstant is too far in the past %s",
                    issueInstantName,
                    PeriodFormat.getDefault().print(timeSinceCreation.toPeriod()))
            );
        }

        if (!dateTimeComparator.isBeforeNowFuzzy(issueInstant)) {
            throw new SamlResponseValidationException(String.format("%s IssueInstant is in the future %s",
                    issueInstantName,
                    issueInstant.withZone(UTC).toString(dateHourMinuteSecond()))
            );
        }
    }
}
