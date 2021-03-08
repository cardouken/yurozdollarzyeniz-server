package ee.uustal.yurozdollarzyeniz.controller.api.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrackingResponse {

    private BigDecimal earned;
    private BigDecimal earnedToday;
    private BigDecimal hourlyRate;
    private LocalDate salaryPeriodStart;
    private long hoursWorked;
    private long daysUntilSalary;

    public BigDecimal getEarned() {
        return earned;
    }

    public TrackingResponse setEarned(BigDecimal earned) {
        this.earned = earned;
        return this;
    }

    public BigDecimal getEarnedToday() {
        return earnedToday;
    }

    public TrackingResponse setEarnedToday(BigDecimal earnedToday) {
        this.earnedToday = earnedToday;
        return this;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public TrackingResponse setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
        return this;
    }

    public LocalDate getSalaryPeriodStart() {
        return salaryPeriodStart;
    }

    public TrackingResponse setSalaryPeriodStart(LocalDate salaryPeriodStart) {
        this.salaryPeriodStart = salaryPeriodStart;
        return this;
    }

    public long getHoursWorked() {
        return hoursWorked;
    }

    public TrackingResponse setHoursWorked(long hoursWorked) {
        this.hoursWorked = hoursWorked;
        return this;
    }

    public long getDaysUntilSalary() {
        return daysUntilSalary;
    }

    public TrackingResponse setDaysUntilSalary(long daysUntilSalary) {
        this.daysUntilSalary = daysUntilSalary;
        return this;
    }
}
