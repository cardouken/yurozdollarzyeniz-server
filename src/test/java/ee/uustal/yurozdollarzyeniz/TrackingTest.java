package ee.uustal.yurozdollarzyeniz;

import ee.uustal.yurozdollarzyeniz.util.TimeProviderSpyHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class TrackingTest extends BaseTest {

    @Autowired
    private TimeProviderSpyHelper timeProvider;

    private static final LocalDateTime WORKING_HOURS = LocalDateTime.of(2021, 3, 3, 12, 0);
    private static final LocalDateTime BEFORE_WORKING_HOURS = LocalDateTime.of(2021, 3, 3, 8, 0);
    private static final LocalDateTime AFTER_WORKING_HOURS = LocalDateTime.of(2021, 3, 3, 19, 0);
    private static final LocalDateTime WEEKEND = LocalDateTime.of(2021, 3, 7, 12, 0);

    @Test
    public void test_api_fields() {
        // when -> then during work hours
        timeProvider.tamperTime(WORKING_HOURS);
        getTracking().buildApi()
                .assertThat("earnedToday", "29.35")
                .assertThat("earned", "264.13")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "18")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28T00:00:00.000Z");

        // when -> then before work hours
        timeProvider.tamperTime(BEFORE_WORKING_HOURS);
        getTracking().buildApi()
                .assertNotExists("earnedToday")
                .assertThat("earned", "234.78")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "16")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28T00:00:00.000Z");

        // when -> then after work hours
        timeProvider.tamperTime(AFTER_WORKING_HOURS);
        getTracking().buildApi()
                .assertNotExists("earnedToday")
                .assertThat("earned", "352.17")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "24")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28T00:00:00.000Z");

        // when -> then during weekends
        timeProvider.tamperTime(WEEKEND);
        getTracking().buildApi()
                .assertNotExists("earnedToday")
                .assertThat("earned", "586.96")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "40")
                .assertThat("daysUntilSalary", "21")
                .assertThat("salaryPeriodStart", "2021-02-28T00:00:00.000Z");
    }

    @Test
    public void next_salary_payment_this_month() {
        // given
        timeProvider.tamperTime(WORKING_HOURS);

        // when -> then
        getTracking().salaryDate(28).buildApi()
                .assertThat("earned", "264.13")
                .assertThat("earnedToday", "29.35")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "18")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28T00:00:00.000Z");
    }

    @Test
    public void next_salary_payment_next_month() {
        // given
        timeProvider.tamperTime(AFTER_WORKING_HOURS);

        // when -> then
        getTracking().salaryDate(1).buildApi()
                .assertThat("hoursWorked", "24")
                .assertThat("daysUntilSalary", "29")
                .assertThat("salaryPeriodStart", "2021-03-01T00:00:00.000Z");
    }

    @Test
    public void test_leap_year() {
        // given
        timeProvider.tamperTime(LocalDateTime.of(2024, 2, 28, 20, 0));

        // when -> then
        getTracking().salaryDate(1).buildApi()
                .assertThat("hoursWorked", "152")
                .assertThat("daysUntilSalary", "2")
                .assertThat("salaryPeriodStart", "2024-02-01T00:00:00.000Z");
    }

    @Test
    public void test_working_hours_estonia() {
        // given
        timeProvider.tamperTime(LocalDateTime.of(2021, 6, 30, 23, 59));

        // when -> then with Estonian locale which has 1 shortened day by 3 hours
        getTracking().salaryDate(1).locale("EE").buildApi()
                .assertThat("hoursWorked", "157");

        // when -> without locale
        getTracking().salaryDate(1).buildApi()
                .assertThat("hoursWorked", "160");
    }

}
