package ee.uustal.yurozdollarzyeniz.service;

import ee.uustal.yurozdollarzyeniz.config.TimeProvider;
import ee.uustal.yurozdollarzyeniz.controller.api.request.TrackingRequest;
import ee.uustal.yurozdollarzyeniz.controller.api.response.TrackingResponse;
import ee.uustal.yurozdollarzyeniz.service.http.DefaultCalendarificService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class TrackingService {

    private final TimeProvider timeProvider;
    private final DefaultCalendarificService calendarificService;

    public TrackingService(TimeProvider timeProvider, DefaultCalendarificService calendarificService) {
        this.timeProvider = timeProvider;
        this.calendarificService = calendarificService;
    }

    public TrackingResponse track(TrackingRequest request) {
        final int salaryDate = request.getSalaryDate();
        final int workDayStartHour = request.getWorkDayStartHour();
        final int workDayLength = request.getWorkDayLengthInHours();
        final int workDayEndHour = workDayStartHour + workDayLength;
        final Double overtimeMultiplier = Optional.ofNullable(request.getOvertimeMultiplier()).orElse(1.5);
        final LocalDateTime dateTimeNow = timeProvider.dateTimeNow();
        final LocalDate dateNow = timeProvider.dateNow();

        final List<LocalDate> weekDayHolidays = Optional.ofNullable(request.getLocale())
                .map(locale -> calendarificService.getHolidays(locale, dateNow.getYear()))
                .orElse(new ArrayList<>());

        final Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        final Predicate<LocalDate> isHoliday = date -> weekDayHolidays.stream().anyMatch(holiday -> Objects.equals(holiday, date));
        final Predicate<LocalDateTime> isWorkingHours = date -> date.getHour() >= workDayStartHour && date.getHour() < workDayEndHour;

        long workDaysInMonth = getDaysWorked(dateNow.withDayOfMonth(1), dateTimeNow.getMonth().length(dateNow.isLeapYear()), isWeekend, isHoliday);
        if (dateNow.getDayOfMonth() >= salaryDate) {
            workDaysInMonth = getDaysWorked(dateNow.plusMonths(1).withDayOfMonth(1), dateTimeNow.getMonth().plus(1).length(dateNow.isLeapYear()), isWeekend, isHoliday);
        }
        final long shortDayHours = getShortDayHours(request.getLocale(), weekDayHolidays);
        final long workHoursInMonth = getHoursWorked(workDaysInMonth, workDayLength, shortDayHours);

        LocalDate lastSalaryPaymentDate = dateNow.withDayOfMonth(salaryDate);
        long daysSinceLastSalary;
        if (dateTimeNow.getDayOfMonth() < salaryDate) {
            lastSalaryPaymentDate = lastSalaryPaymentDate.minusMonths(1);
        }
        daysSinceLastSalary = ChronoUnit.DAYS.between(lastSalaryPaymentDate, dateTimeNow.plusDays(1));

        long daysWorked = getDaysWorked(lastSalaryPaymentDate, daysSinceLastSalary, isWeekend, isHoliday);
        long actualDaysWorked = getDaysWorked(lastSalaryPaymentDate, ChronoUnit.DAYS.between(lastSalaryPaymentDate, lastSalaryPaymentDate.plusMonths(1)), isWeekend, isHoliday);

        long hoursWorked = getHoursWorked(daysWorked, workDayLength, shortDayHours);
        long actualHoursWorked = getHoursWorked(actualDaysWorked, workDayLength, shortDayHours);
        final double actualHourlySalary = request.getMonthlySalary() / actualHoursWorked;

        BigDecimal earnedOvertime = null;
        if (request.getOvertimeHours() != null) {
            final double hourlySalary = request.getMonthlySalary() / workHoursInMonth;
            earnedOvertime = BigDecimal.valueOf(request.getOvertimeHours() * hourlySalary * overtimeMultiplier);
        }

        BigDecimal earnedTotal;
        BigDecimal earnedToday = null;
        final boolean isWorkTime = isWorkingHours.test(dateTimeNow) && !isWeekend.test(dateNow) && !isHoliday.test(dateNow);
        if (isWorkTime) {
            hoursWorked -= workDayLength;
            final LocalDateTime dayStart = dateTimeNow.withHour(workDayStartHour).truncatedTo(ChronoUnit.HOURS);

            final long secondsWorkedToday = ChronoUnit.SECONDS.between(dayStart, dateTimeNow);
            earnedToday = BigDecimal.valueOf(actualHourlySalary / 60 / 60 * secondsWorkedToday).setScale(2, RoundingMode.HALF_UP);
            earnedTotal = earnedToday.add(BigDecimal.valueOf(hoursWorked * actualHourlySalary));
            hoursWorked += ChronoUnit.HOURS.between(dayStart, dateTimeNow);

        } else {
            if (!isWeekend.test(dateNow) && dateTimeNow.isBefore(dateTimeNow.withHour(workDayStartHour).withMinute(0).withSecond(0))) {
                hoursWorked -= workDayLength;
                earnedToday = BigDecimal.ZERO;
            }
            earnedTotal = BigDecimal.valueOf(actualHourlySalary * hoursWorked);
            if (!isWorkingHours.test(dateTimeNow) && !isWeekend.test(dateNow) && dateTimeNow.isAfter(dateTimeNow.withHour(workDayEndHour - 1).withMinute(59).withSecond(59))) {
                earnedToday = BigDecimal.valueOf(actualHourlySalary * workDayLength).setScale(2, RoundingMode.HALF_UP);
            }
        }
        if (earnedOvertime != null) {
            earnedTotal = earnedTotal.add(earnedOvertime);
        }

        // salary should be paid out on the last business day in case payment date is a weekend or a holiday
        LocalDate nextSalaryPaymentDate = lastSalaryPaymentDate.plusMonths(1);
        while (isWeekend.test(nextSalaryPaymentDate) || isHoliday.test(nextSalaryPaymentDate)) {
            nextSalaryPaymentDate = nextSalaryPaymentDate.minusDays(1);
        }
        while (isWeekend.test(lastSalaryPaymentDate) || isHoliday.test(lastSalaryPaymentDate)) {
            lastSalaryPaymentDate = lastSalaryPaymentDate.minusDays(1);
        }

        long daysUntilSalary = dateNow.until(nextSalaryPaymentDate, ChronoUnit.DAYS);
        if (daysUntilSalary == 0 && dateTimeNow.getHour() >= workDayEndHour) {
            nextSalaryPaymentDate = nextSalaryPaymentDate.plusMonths(1).withDayOfMonth(salaryDate);
            while (isWeekend.test(nextSalaryPaymentDate) || isHoliday.test(nextSalaryPaymentDate)) {
                nextSalaryPaymentDate = nextSalaryPaymentDate.minusDays(1);
            }
        }
        daysUntilSalary = dateNow.until(nextSalaryPaymentDate, ChronoUnit.DAYS);

        return new TrackingResponse()
                .setEarned(earnedTotal.setScale(2, RoundingMode.HALF_UP))
                .setEarnedToday(earnedToday)
                .setEarnedOvertime(earnedOvertime)
                .setHourlyRate(BigDecimal.valueOf(actualHourlySalary))
                .setHoursWorked(hoursWorked + Optional.ofNullable(request.getOvertimeHours()).orElse(0))
                .setSalaryPeriodStart(lastSalaryPaymentDate)
                .setDaysUntilSalary(daysUntilSalary)
                .setIsWorkingHours(isWorkTime);
    }

    private long getHoursWorked(long days, int workDayLength, long shortDayHours) {
        return days * workDayLength - shortDayHours;
    }

    private long getDaysWorked(LocalDate startDate, long limit, Predicate<LocalDate> isWeekend, Predicate<LocalDate> isHoliday) {
        return Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(limit)
                .filter(isWeekend.negate())
                .filter(isHoliday.negate())
                .count();
    }

    private long getShortDayHours(String locale, List<LocalDate> weekDayHolidays) {
        final List<LocalDate> shortenedDaysList = List.of(
                LocalDate.of(timeProvider.dateNow().getYear(), 2, 23),
                LocalDate.of(timeProvider.dateNow().getYear(), 6, 22),
                LocalDate.of(timeProvider.dateNow().getYear(), 2, 23),
                LocalDate.of(timeProvider.dateNow().getYear(), 12, 31)
        );

        final Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        long count = 0;
        if (Objects.equals(locale, "EE")) {
            count = weekDayHolidays.stream()
                    .filter(holiday -> holiday.getMonth() == timeProvider.dateNow().getMonth())
                    .filter(holiday -> shortenedDaysList.contains(holiday.minusDays(1)))
                    .filter(holiday -> !isWeekend.test(holiday.minusDays(1)))
                    .count();
        }

        return count * 3;
    }

}
