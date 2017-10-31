package unit.uk.gov.ida.verifyserviceprovider.utils;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeComparatorTest {

    private static final DateTime baseTime = new DateTime(2017, 1, 1, 12, 0);
    private static final DateTimeComparator comparator = new DateTimeComparator(Duration.standardSeconds(5));

    @Test
    public void isAfterReturnsTrueWhenAIsAfterB() throws Exception {
        DateTime newTime = baseTime.plusMinutes(1);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    public void isAfterReturnsFalseWhenAIsBeforeB() throws Exception {
        DateTime newTime = baseTime.minusMinutes(1);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isFalse();
    }

    @Test
    public void isAfterReturnsTrueWhenAIsWithinSkewOfB() throws Exception {
        DateTime newTime = baseTime.minusSeconds(4);

        assertThat(comparator.isAfterFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    public void isBeforeReturnsTrueWhenAIsBeforeB() throws Exception {
        DateTime newTime = baseTime.minusMinutes(1);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    public void isBeforeReturnsFalseWhenAIsAfterB() throws Exception {
        DateTime newTime = baseTime.plusMinutes(1);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isFalse();
    }

    @Test
    public void isBeforeReturnsTrueWhenAIsWithinSkewOfB() throws Exception {
        DateTime newTime = baseTime.plusSeconds(4);

        assertThat(comparator.isBeforeFuzzy(newTime, baseTime)).isTrue();
    }

    @Test
    public void isBeforeNowReturnsTrueWhenInThePast() {
        DateTime pastDateTime = new DateTime().minusMinutes(1);

        assertThat(comparator.isBeforeNow(pastDateTime)).isTrue();
    }

    @Test
    public void isBeforeNowReturnsFalseWhenInThePast() {
        DateTime dateTime = new DateTime().plusMillis(1);

        assertThat(comparator.isBeforeNow(dateTime)).isFalse();
    }

    @Test
    public void isAfterNowReturnsTrueWhenInTheFuture() {
        DateTime futureDateTime = new DateTime().plusMinutes(1);

        assertThat(comparator.isAfterNow(futureDateTime)).isTrue();
    }

    @Test
    public void isAfterNowReturnsFalseWhenInThePast() {
        DateTime pastDateTime = new DateTime().minusMillis(1);

        assertThat(comparator.isAfterNow(pastDateTime)).isFalse();
    }
}