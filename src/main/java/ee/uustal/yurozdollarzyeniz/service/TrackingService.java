package ee.uustal.yurozdollarzyeniz.service;

import ee.uustal.yurozdollarzyeniz.config.TimeProvider;
import ee.uustal.yurozdollarzyeniz.controller.api.request.TrackingRequest;
import ee.uustal.yurozdollarzyeniz.controller.api.response.TrackingResponse;
import ee.uustal.yurozdollarzyeniz.pojo.Holiday;
import ee.uustal.yurozdollarzyeniz.service.http.DefaultCalendarificService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class TrackingService {

    private final TimeProvider timeProvider;
    private final DefaultCalendarificService calendarificService;
    private static final List<String> SHORTENED_DAY_LIST = List.of("Independence Day", "Victory Day", "Christmas Eve");

    public TrackingService(TimeProvider timeProvider, DefaultCalendarificService calendarificService) {
        this.timeProvider = timeProvider;
        this.calendarificService = calendarificService;
    }

    public TrackingResponse track(TrackingRequest request) {
        final Predicate<LocalDateTime> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        final List<Holiday> weekDayHolidays = calendarificService.getHolidays(request.getLocale(), LocalDateTime.now().getYear());
        final LocalDateTime now = timeProvider.now();

        final int workDayHours = request.getWorkDayLengthInHours();
        long shortenedDayHours = 0;
        if (Objects.equals(request.getLocale(), "EE")) {
            shortenedDayHours = weekDayHolidays.stream()
                    .filter(weekDayHoliday -> weekDayHoliday.getDate().getMonth() == now.getMonth())
                    .filter(weekDayHoliday -> SHORTENED_DAY_LIST.contains(weekDayHoliday.getName()))
                    .count();
        }
        final long excludedHolidays = weekDayHolidays.stream()
                .filter(d -> d.getDate().getMonth() == now.getMonth())
                .count();
        long workDaysInMonth = Stream.iterate(now.withDayOfMonth(1), date -> date.plusDays(1))
                .limit(now.getMonth().length(LocalDate.now().isLeapYear()))
                .filter(isWeekend.negate())
                .count() - excludedHolidays;

        long workHoursInMonth = workDaysInMonth * workDayHours - shortenedDayHours * 3;
        final int salaryDate = request.getSalaryDate();
        final int workDayStartHour = request.getWorkDayStartHour();
        final int workDayEndHour = workDayStartHour + workDayHours;
        final double hourlySalary = request.getMonthlySalary() / workHoursInMonth;

        final Predicate<LocalDateTime> isWorkingHours = date -> date.getHour() >= workDayStartHour && date.getHour() <= workDayEndHour;

        LocalDateTime lastSalaryPaymentDate = timeProvider.now();
        lastSalaryPaymentDate = lastSalaryPaymentDate.withDayOfMonth(salaryDate);
        long daysSinceLastSalary;
        if (now.getDayOfMonth() < salaryDate) {
            lastSalaryPaymentDate = lastSalaryPaymentDate.minusMonths(1);
        }
        daysSinceLastSalary = ChronoUnit.DAYS.between(lastSalaryPaymentDate, now.plusDays(1));

        long hoursWorked = Stream.iterate(lastSalaryPaymentDate, date -> date.plusDays(1))
                .limit(daysSinceLastSalary)
                .filter(isWeekend.negate())
                .count() * workDayHours;
        hoursWorked = ((hoursWorked - (excludedHolidays * 8) - (shortenedDayHours * 3)));

        BigDecimal earnedTotal;
        BigDecimal earnedToday = null;

        if (!isWorkingHours.test(now) || isWeekend.test(now)) {
            if (!isWeekend.test(now) && now.isBefore(now.withHour(workDayStartHour))) {
                hoursWorked -= workDayHours;
            }
            earnedTotal = BigDecimal.valueOf(hourlySalary * hoursWorked);
        } else {
            hoursWorked = (hoursWorked - workDayHours);
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
