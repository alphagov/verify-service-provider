package uk.gov.ida.verifyserviceprovider.utils;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class DateTimeComparator {

    public DateTimeComparator(Duration clockSkew) {
        this.clockSkew = clockSkew;
    }

    private final Duration clockSkew;

    public boolean isAfterFuzzy(DateTime source, DateTime target) {
        return source.isAfter(target.minus(clockSkew));
    }

    public boolean isBeforeFuzzy(DateTime source, DateTime target) {
        return source.isBefore(target.plus(clockSkew));
    }

    public boolean isDefinitelyBeforeNow(DateTime dateTime) {
        return !isBeforeFuzzy(DateTime.now(), dateTime);
    }

    public boolean isDefinitelyAfterNow(DateTime dateTime) {
        return !isAfterFuzzy(DateTime.now(), dateTime);
    }

    public boolean isAfterNowFuzzy(DateTime dateTime) {
        return isAfterFuzzy(dateTime, DateTime.now());
    }

    public boolean isBeforeNowFuzzy(DateTime dateTime) {
        return isBeforeFuzzy(dateTime, DateTime.now());
    }
}
