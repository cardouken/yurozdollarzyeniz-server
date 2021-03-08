package ee.uustal.yurozdollarzyeniz;

import ee.uustal.yurozdollarzyeniz.util.TimeProviderSpyHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class TrackingTest extends BaseTest {

    @Autowired
    private TimeProviderSpyHelper timeProvider;

    private static final LocalDateTime WORK_HOURS = LocalDateTime.of(2021, 3, 3, 12, 0);
    private static final LocalDateTime BEFORE_WORK_HOURS = LocalDateTime.of(2021, 3, 3, 8, 0);
    private static final LocalDateTime AFTER_WORK_HOURS = LocalDateTime.of(2021, 3, 3, 19, 0);
    private static final LocalDateTime WEEKEND = LocalDateTime.of(2021, 3, 7, 12, 0);

    @Test
    public void track_during_work_hours() {
        // given
        timeProvider.tamperTime(WORK_HOURS);

        // when -> then
        getTracking().buildApi()
                .assertThat("earnedToday", "29.35")
                .assertThat("earned", "264.13")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "18")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28");
    }

    @Test
    public void track_before_work_hours() {
        // given
        timeProvider.tamperTime(BEFORE_WORK_HOURS);

        // when -> then
        getTracking().buildApi()
                .assertNotExists("earnedToday")
                .assertThat("earned", "234.78")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "16")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28");
    }

    @Test
    public void track_after_work_hours() {
        // given
        timeProvider.tamperTime(AFTER_WORK_HOURS);

        // when -> then
        getTracking().buildApi()
                .assertNotExists("earnedToday")
                .assertThat("earned", "352.17")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "24")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28");
    }

    @Test
    public void track_during_weekend() {
        // given
        timeProvider.tamperTime(WEEKEND);

        // when -> then
        getTracking().buildApi()
                .assertNotExists("earnedToday")
                .assertThat("earned", "586.96")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "40")
                .assertThat("daysUntilSalary", "21")
                .assertThat("salaryPeriodStart", "2021-02-28");
    }

    @Test
    public void next_salary_payment_this_month() {
        // given
        timeProvider.tamperTime(WORK_HOURS);

        // when -> then
        getTracking().salaryDate(28).buildApi()
                .assertThat("earned", "264.13")
                .assertThat("earnedToday", "29.35")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "18")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28");
    }

    @Test
    public void next_salary_payment_next_month() {
        // given
        timeProvider.tamperTime(AFTER_WORK_HOURS);

        // when -> then
        getTracking().salaryDate(1).buildApi()
                .assertThat("hoursWorked", "24")
                .assertThat("daysUntilSalary", "29")
                .assertThat("salaryPeriodStart", "2021-03-01");
    }

    @Test
    public void test_leap_year() {
        // given
        timeProvider.tamperTime(LocalDateTime.of(2024, 2, 28, 20, 0));

        // when -> then
        getTracking().salaryDate(1).locale("EE").buildApi()
                .assertThat("hoursWorked", "157")
                .assertThat("daysUntilSalary", "2")
                .assertThat("salaryPeriodStart", "2024-02-01");
    }

    @Test
    public void test_working_hours_with_estonian_locale() {
        // given
        timeProvider.tamperTime(LocalDateTime.of(2021, 6, 30, 23, 59));

        // when -> then with Estonian locale which has 1 shortened day by 3 hours
        getTracking().salaryDate(1).locale("EE").buildApi()
                .assertThat("hoursWorked", "157");

        // when -> without locale
        getTracking().salaryDate(1).buildApi()
                .assertThat("hoursWorked", "176");
    }

    @Test
    public void test_overtime_hours() {
        // given
        timeProvider.tamperTime(WORK_HOURS);

        // when -> then
        getTracking().overtimeHours(10).buildApi()
                .assertThat("earnedToday", "29.35")
                .assertThat("earnedOvertime", "220.11")
                .assertThat("earned", "484.24")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "18")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28");
    }

}
