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

        long workDaysInMonth = Stream.iterate(dateNow.withDayOfMonth(1), date -> date.plusDays(1))
                .limit(dateTimeNow.getMonth().length(dateNow.isLeapYear()))
                .filter(isWeekend.negate())
                .filter(isHoliday.negate())
                .count();
        long shortDayHours = getShortDayHours(request.getLocale(), weekDayHolidays);
        long workHoursInMonth = workDaysInMonth * workDayLength - shortDayHours;

        LocalDate lastSalaryPaymentDate = dateNow;
        lastSalaryPaymentDate = lastSalaryPaymentDate.withDayOfMonth(salaryDate);
        long daysSinceLastSalary;
        if (dateTimeNow.getDayOfMonth() < salaryDate) {
            lastSalaryPaymentDate = lastSalaryPaymentDate.minusMonths(1);
        }
        daysSinceLastSalary = ChronoUnit.DAYS.between(lastSalaryPaymentDate, dateTimeNow.plusDays(1));

        long daysWorked = Stream.iterate(lastSalaryPaymentDate, date -> date.plusDays(1))
                .limit(daysSinceLastSalary)
                .filter(isWeekend.negate())
                .filter(isHoliday.negate())
                .count();

        final double hourlySalary = request.getMonthlySalary() / workHoursInMonth;
        BigDecimal earnedOvertime = null;
        if (request.getOvertimeHours() != null) {
            earnedOvertime = BigDecimal.valueOf(request.getOvertimeHours() * hourlySalary * overtimeMultiplier);
        }
        long hoursWorked = daysWorked * workDayLength - shortDayHours;

        BigDecimal earnedTotal;
        BigDecimal earnedToday = null;
        if (!isWorkingHours.test(dateTimeNow) || isWeekend.test(dateNow) || isHoliday.test(dateNow)) {
            if (!isWeekend.test(dateNow) && dateTimeNow.isBefore(dateTimeNow.withHour(workDayStartHour))) {
                hoursWorked -= workDayLength;
            }
            earnedTotal = BigDecimal.valueOf(hourlySalary * hoursWorked);
        } else {
            hoursWorked -= workDayLength;
            final LocalDateTime dayStart = dateTimeNow.withHour(workDayStartHour).truncatedTo(ChronoUnit.HOURS);

            final long secondsWorkedToday = ChronoUnit.SECONDS.between(dayStart, dateTimeNow);
            earnedToday = BigDecimal.valueOf(hourlySalary / 60 / 60 * secondsWorkedToday).setScale(2, RoundingMode.HALF_UP);
            earnedTotal = earnedToday.add(BigDecimal.valueOf(hoursWorked * hourlySalary));
            hoursWorked += ChronoUnit.HOURS.between(dayStart, dateTimeNow);
        }
        if (earnedOvertime != null) {
            earnedTotal = earnedTotal.add(earnedOvertime);
        }

        return new TrackingResponse()
                .setEarned(earnedTotal.setScale(2, RoundingMode.HALF_UP))
                .setEarnedToday(earnedToday)
                .setEarnedOvertime(earnedOvertime)
                .setHourlyRate(BigDecimal.valueOf(hourlySalary))
                .setHoursWorked(hoursWorked + Optional.ofNullable(request.getOvertimeHours()).orElse(0))
                .setSalaryPeriodStart(lastSalaryPaymentDate)
                .setDaysUntilSalary(dateNow.until(lastSalaryPaymentDate.plusMonths(1), ChronoUnit.DAYS));
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
