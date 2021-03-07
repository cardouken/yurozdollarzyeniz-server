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
        final int salaryDate = request.getSalaryDate();
        final int workDayStartHour = request.getWorkDayStartHour();
        final int workDayLength = request.getWorkDayLengthInHours();
        final int workDayEndHour = workDayStartHour + workDayLength;
        final LocalDateTime now = timeProvider.now();

        final List<Holiday> weekDayHolidays = calendarificService.getHolidays(request.getLocale(), LocalDateTime.now().getYear());

        final Predicate<LocalDateTime> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        final Predicate<LocalDateTime> isHoliday = date -> weekDayHolidays.stream().anyMatch(wdh -> Objects.equals(wdh.getDate(), date));
        final Predicate<LocalDateTime> isWorkingHours = date -> date.getHour() >= workDayStartHour && date.getHour() <= workDayEndHour;

        long workDaysInMonth = Stream.iterate(now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS), date -> date.plusDays(1))
                .limit(now.getMonth().length(LocalDate.now().isLeapYear()))
                .filter(isWeekend.negate())
                .filter(isHoliday.negate())
                .count();
        long shortDayHours = getShortDayHours(request.getLocale(), now, weekDayHolidays);
        long workHoursInMonth = workDaysInMonth * workDayLength - shortDayHours * 3;
        final double hourlySalary = request.getMonthlySalary() / workHoursInMonth;

        LocalDateTime lastSalaryPaymentDate = timeProvider.now();
        lastSalaryPaymentDate = lastSalaryPaymentDate.withDayOfMonth(salaryDate);
        long daysSinceLastSalary;
        if (now.getDayOfMonth() < salaryDate) {
            lastSalaryPaymentDate = lastSalaryPaymentDate.minusMonths(1);
        }
        daysSinceLastSalary = ChronoUnit.DAYS.between(lastSalaryPaymentDate, now.plusDays(1));

        long daysWorked = Stream.iterate(lastSalaryPaymentDate.truncatedTo(ChronoUnit.DAYS), date -> date.plusDays(1))
                .limit(daysSinceLastSalary)
                .filter(isWeekend.negate())
                .filter(isHoliday.negate())
                .count();
        long hoursWorked = daysWorked * workDayLength - shortDayHours * 3;

        BigDecimal earnedTotal;
        BigDecimal earnedToday = null;
        if (!isWorkingHours.test(now) || isWeekend.test(now) || isHoliday.test(now)) {
            if (!isWeekend.test(now) && now.isBefore(now.withHour(workDayStartHour))) {
                hoursWorked -= workDayLength;
            }
            earnedTotal = BigDecimal.valueOf(hourlySalary * hoursWorked);
        } else {
            hoursWorked = (hoursWorked - workDayLength);
            final LocalDateTime dayStart = now.withHour(workDayStartHour);

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

    private long getShortDayHours(String locale, LocalDateTime now, List<Holiday> weekDayHolidays) {
        long shortDayHours = 0;
        if (Objects.equals(locale, "EE")) {
            shortDayHours = weekDayHolidays.stream()
                    .filter(weekDayHoliday -> weekDayHoliday.getDate().getMonth() == now.getMonth())
                    .filter(weekDayHoliday -> SHORTENED_DAY_LIST.contains(weekDayHoliday.getName()))
                    .count();
        }
        return shortDayHours;
    }

}
