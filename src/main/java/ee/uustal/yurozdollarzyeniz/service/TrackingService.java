package ee.uustal.yurozdollarzyeniz.service;

import ee.uustal.yurozdollarzyeniz.controller.api.response.TrackingResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class TrackingService {

    private static final int WORKING_HOURS = 184;
    private static final double SALARY = 2700;
    private static final int START_HOUR = 10;
    private static final int END_HOUR = 18;


    public TrackingResponse track() {
        final LocalDateTime salaryStartDate = LocalDateTime.of(2021, 2, 25, 15, 0);
        final LocalDateTime now = LocalDateTime.now();

        final Predicate<LocalDateTime> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        final Predicate<LocalDateTime> isWorkingHours = date -> date.getHour() >= START_HOUR && date.getHour() <= END_HOUR;
        final long daysFromSalary = ChronoUnit.DAYS.between(salaryStartDate, now);

        long businessDays = Stream.iterate(salaryStartDate, date -> date.plusDays(1))
                .limit(daysFromSalary)
                .filter(isWeekend.negate())
                .count();

        final double hourlyRate = SALARY / WORKING_HOURS;
        final long hoursWorked = businessDays * 8;
        double earned;

        double earnedToday = 0;
        if (!isWorkingHours.test(LocalDateTime.now()) ) {
            earned = hourlyRate * hoursWorked;
        } else {
            final long hoursWorkedSoFar = (businessDays - 1) * 8;
            final LocalDateTime dayStart = LocalDateTime.now().withHour(START_HOUR);

            final long workedToday = ChronoUnit.SECONDS.between(dayStart, LocalDateTime.now());
            earnedToday = hourlyRate / 60 / 60 * workedToday;
            earned = (hoursWorkedSoFar * hourlyRate) + earnedToday;
        }

        return new TrackingResponse()
                .setEarned(BigDecimal.valueOf(earned).setScale(2, RoundingMode.HALF_UP))
                .setEarnedToday(BigDecimal.valueOf(earnedToday).setScale(2, RoundingMode.HALF_UP));
    }
}
