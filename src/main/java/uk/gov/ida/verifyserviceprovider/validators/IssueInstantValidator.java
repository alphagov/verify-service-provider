package uk.gov.ida.verifyserviceprovider.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.PeriodFormat;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;

public class IssueInstantValidator {

    private static final Duration ISSUE_INSTANT_DURATION = Duration.standardMinutes(5);
    private final String issueInstantName;

    public IssueInstantValidator(String issueInstantName) {
        this.issueInstantName = issueInstantName;
    }

    public void validate(DateTime issueInstant) {
        Duration timeSinceCreation = new Duration(issueInstant, Instant.now());
        if (timeSinceCreation.isLongerThan(ISSUE_INSTANT_DURATION)) {
            throw new SamlResponseValidationException(String.format("%s IssueInstant is too far in the past %s",
                    issueInstantName,
                    PeriodFormat.getDefault().print(timeSinceCreation.toPeriod()))
            );
        }

        if (issueInstant.isAfter(Instant.now())) {
            throw new SamlResponseValidationException(String.format("%s IssueInstant is in the future %s",
                    issueInstantName,
                    issueInstant.withZone(UTC).toString(dateHourMinuteSecond()))
            );
        }
    }
}
