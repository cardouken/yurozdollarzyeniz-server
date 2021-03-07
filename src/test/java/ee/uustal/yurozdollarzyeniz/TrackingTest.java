package ee.uustal.yurozdollarzyeniz;

import ee.uustal.yurozdollarzyeniz.util.TimeProviderSpyHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class TrackingTest extends BaseTest {

    @Autowired
    private TimeProviderSpyHelper timeProvider;

    private static final LocalDateTime WORKING_HOURS = LocalDateTime.of(2021, 3, 3, 12, 0);
    private static final LocalDateTime NOT_WORKING_HOURS = LocalDateTime.of(2021, 3, 3, 19, 0);
    private static final LocalDateTime WEEKEND = LocalDateTime.of(2021, 3, 7, 12, 0);

    @Test
    public void test_api_fields() {
        // given
        double salary = 2700;
        int salaryPeriodStartDay = 28;
        int salaryPeriodStartDay = 28;


        // when -> then during work hours
        timeProvider.tamperTime(WORKING_HOURS);
        getTracking().setMonthlySalary(salary).setSalaryPeriodStartDay(salaryPeriodStartDay).buildApi()
                .assertExists("earned")
                .assertExists("earnedToday")
                .assertExists("hourlyRate")
                .assertExists("hoursWorked")
                .assertExists("daysUntilSalary")
                .assertExists("salaryPeriodStart");

        // when -> then outside of work hours
        timeProvider.tamperTime(NOT_WORKING_HOURS);
        getTracking().buildApi()
                .assertNotExists("earnedToday")
                .assertExists("earned")
                .assertExists("hourlyRate")
                .assertExists("hoursWorked")
                .assertExists("daysUntilSalary")
                .assertExists("salaryPeriodStart");

        // when -> then during weekends
        timeProvider.tamperTime(WEEKEND);
        getTracking().buildApi()
                .assertNotExists("earnedToday")
                .assertExists("earned")
                .assertExists("hourlyRate")
                .assertExists("hoursWorked")
                .assertExists("daysUntilSalary")
                .assertExists("salaryPeriodStart");
    }

    @Test
    public void salary_start_date_in_last_month() {
        // given
        timeProvider.tamperTime(WORKING_HOURS);

        // when -> then
        getTracking().buildApi()
                .assertThat("earned", "264.13")
                .assertThat("earnedToday", "29.35")
                .assertThat("hourlyRate", "14.67")
                .assertThat("hoursWorked", "18")
                .assertThat("daysUntilSalary", "25")
                .assertThat("salaryPeriodStart", "2021-02-28T00:00:00.000Z");
    }

    @Test
    public void salary_start_date_in_current_month() {
        // given
        timeProvider.tamperTime(WORKING_HOURS);

        // when -> then
        getTracking().setSalaryPeriodStartDay(10).buildApi()
                .assertThat("daysUntilSalary", "7")
                .assertThat("salaryPeriodStart", "2021-02-10T00:00:00.000Z");
    }
}
