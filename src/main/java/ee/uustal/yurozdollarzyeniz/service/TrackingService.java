package ee.uustal.yurozdollarzyeniz.service;

import ee.uustal.yurozdollarzyeniz.config.TimeProvider;
import ee.uustal.yurozdollarzyeniz.controller.api.request.TrackingRequest;
import ee.uustal.yurozdollarzyeniz.controller.api.response.TrackingResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class TrackingService {

    private final TimeProvider timeProvider;

    public TrackingService(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TrackingResponse track(TrackingRequest request) {
        final int salaryDate = request.getSalaryDate();
        final int workDayStartHour = request.getWorkDayStartHour();
        final int workDayHours = request.getWorkDayLengthInHours();
        final int workDayEndHour = workDayStartHour + workDayHours;
        final double hourlySalary = request.getMonthlySalary() / request.getWorkingHoursInMonth();
        final LocalDateTime now = timeProvider.now();

        final Predicate<LocalDateTime> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        final Predicate<LocalDateTime> isWorkingHours = date -> date.getHour() >= workDayStartHour && date.getHour() <= workDayEndHour;

        LocalDateTime lastSalaryPaymentDate = timeProvider.now();
        lastSalaryPaymentDate = lastSalaryPaymentDate.withDayOfMonth(salaryDate);
        long daysSinceLastSalary;
        if (now.getDayOfMonth() < salaryDate) {
            lastSalaryPaymentDate = lastSalaryPaymentDate.minusMonths(1);
        }
        daysSinceLastSalary = ChronoUnit.DAYS.between(lastSalaryPaymentDate, now.plusDays(1));

        long businessDaysWorked = Stream.iterate(lastSalaryPaymentDate, date -> date.plusDays(1))
                .limit(daysSinceLastSalary)
                .filter(isWeekend.negate())
                .count();

        long hoursWorked = businessDaysWorked * workDayHours;
        BigDecimal earnedTotal;
        BigDecimal earnedToday = null;

        if (!isWorkingHours.test(now) || isWeekend.test(now)) {
            if (!isWeekend.test(now) && now.isBefore(now.withHour(workDayStartHour))) {
                hoursWorked -= workDayHours;
            }
            earnedTotal = BigDecimal.valueOf(hourlySalary * hoursWorked);
        } else {
            hoursWorked = (businessDaysWorked - 1) * workDayHours;
            final LocalDateTime dayStart = timeProvider.now().withHour(workDayStartHour);

            final long secondsWorkedToday = ChronoUnit.SECONDS.between(dayStart, now);
            earnedToday = BigDecimal.valueOf(hourlySalary / 60 / 60 * secondsWorkedToday);
            earnedTotal = earnedToday.add(BigDecimal.valueOf(hoursWorked * hourlySalary));
            hoursWorked += ChronoUnit.HOURS.between(dayStart, now);
        }

        long daysUntilNextSalary = 0;
        if (lastSalaryPaymentDate.getMonthValue() < now.getMonthValue() || lastSalaryPaymentDate.getMonth() == now.getMonth()) {
            daysUntilNextSalary = now.until(lastSalaryPaymentDate.plusMonths(1), ChronoUnit.DAYS);
        }

        return new TrackingResponse()
                .setEarned(earnedTotal)
                .setEarnedToday(earnedToday)
                .setHourlyRate(BigDecimal.valueOf(hourlySalary))
                .setHoursWorked(hoursWorked)
                .setSalaryPeriodStart(lastSalaryPaymentDate.truncatedTo(ChronoUnit.DAYS))
                .setDaysUntilSalary(daysUntilNextSalary);
    }
}
