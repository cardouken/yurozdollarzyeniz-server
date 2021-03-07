package ee.uustal.yurozdollarzyeniz.service;

import ee.uustal.yurozdollarzyeniz.config.TimeProvider;
import ee.uustal.yurozdollarzyeniz.controller.api.request.TrackingRequest;
import ee.uustal.yurozdollarzyeniz.controller.api.response.TrackingResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class TrackingService {

    private final TimeProvider timeProvider;

//    private static final int WORKING_HOURS = 184;
//    private static final int WORKDAY_START_HOUR = 10;
//    private static final int WORKDAY_END_HOUR = 18;
//    private static final int SALARY_PERIOD_START_DAY = 28;
//    private static final double SALARY = 2700;
//    private static final double HOURLY_RATE = SALARY / WORKING_HOURS;

    public TrackingService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TrackingResponse track(TrackingRequest request) {
        final int salaryPeriodStartDay = request.getSalaryPeriodStartDay();
        final int workDayStartHour = request.getWorkDayStartHour();
        final int workDayEndHour = request.getWorkDayEndHour();
        final double hourlyRate = request.getMonthlySalary() / request.getWorkingHoursInMonth();
        final LocalDateTime now = timeProvider.now();

        LocalDateTime salaryPeriodStartDate = timeProvider.now();
        if (now.getDayOfMonth() < salaryPeriodStartDay) {
            salaryPeriodStartDate = salaryPeriodStartDate.with(now.minusMonths(1).withDayOfMonth(salaryPeriodStartDay));
        } else {
            salaryPeriodStartDate = salaryPeriodStartDate.withDayOfMonth(salaryPeriodStartDay);
        }

        final Predicate<LocalDateTime> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        final Predicate<LocalDateTime> isWorkingHours = date -> date.getHour() >= workDayStartHour && date.getHour() <= workDayEndHour;
        final long daysFromSalary = ChronoUnit.DAYS.between(salaryPeriodStartDate, now);

        long businessDaysWorked = Stream.iterate(salaryPeriodStartDate, date -> date.plusDays(1))
                .limit(daysFromSalary)
                .filter(isWeekend.negate())
                .count();

        long hoursWorked = businessDaysWorked * 8;
        BigDecimal earnedTotal;
        BigDecimal earnedToday = null;
        if (!isWorkingHours.test(now) || isWeekend.test(now)) {
            earnedTotal = BigDecimal.valueOf(hourlyRate * hoursWorked);
        } else {
            final long hoursWorkedSoFar = (businessDaysWorked) * 8;
            final LocalDateTime dayStart = timeProvider.now().withHour(workDayStartHour);

            final long workedToday = ChronoUnit.SECONDS.between(dayStart, now);
            hoursWorked = hoursWorkedSoFar + ChronoUnit.HOURS.between(dayStart, now);
            earnedToday = BigDecimal.valueOf(hourlyRate / 60 / 60 * workedToday);
            earnedTotal = earnedToday.add(BigDecimal.valueOf(hoursWorkedSoFar * hourlyRate));
        }

        return new TrackingResponse()
                .setEarned(earnedTotal)
                .setEarnedToday(earnedToday)
                .setHourlyRate(BigDecimal.valueOf(hourlyRate))
                .setHoursWorked(hoursWorked)
                .setSalaryPeriodStart(salaryPeriodStartDate.truncatedTo(ChronoUnit.DAYS))
                .setDaysUntilSalary(now.until(now.withDayOfMonth(salaryPeriodStartDay), ChronoUnit.DAYS));
    }
}
