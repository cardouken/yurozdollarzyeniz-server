package ee.uustal.yurozdollarzyeniz.controller.api.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TrackingRequest {

    @NotNull
    @Min(0)
    @Max(23)
    private int workDayStartHour;

    @NotNull
    @Min(1)
    @Max(24)
    private int workDayLengthInHours;

    @NotNull
    @Min(1)
    @Max(31)
    private int salaryDate;

    @NotNull
    @Min(0)
    private double monthlySalary;

    private String locale;

    private Integer overtimeHours;

    public int getWorkDayStartHour() {
        return workDayStartHour;
    }

    public TrackingRequest setWorkDayStartHour(int workDayStartHour) {
        this.workDayStartHour = workDayStartHour;
        return this;
    }

    public int getWorkDayLengthInHours() {
        return workDayLengthInHours;
    }

    public TrackingRequest setWorkDayLengthInHours(int workDayLengthInHours) {
        this.workDayLengthInHours = workDayLengthInHours;
        return this;
    }

    public int getSalaryDate() {
        return salaryDate;
    }

    public TrackingRequest setSalaryDate(int salaryDate) {
        this.salaryDate = salaryDate;
        return this;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public TrackingRequest setMonthlySalary(double monthlySalary) {
        this.monthlySalary = monthlySalary;
        return this;
    }

    public String getLocale() {
        return locale;
    }

    public TrackingRequest setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public Integer getOvertimeHours() {
        return overtimeHours;
    }

    public TrackingRequest setOvertimeHours(Integer overtimeHours) {
        this.overtimeHours = overtimeHours;
        return this;
    }
}
