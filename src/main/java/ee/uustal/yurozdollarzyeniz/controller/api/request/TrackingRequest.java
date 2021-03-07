package ee.uustal.yurozdollarzyeniz.controller.api.request;

public class TrackingRequest {

    private int workingHoursInMonth;
    private int workDayStartHour;
    private int workDayEndHour;
    private int salaryPeriodStartDay;
    private double monthlySalary;

    public int getWorkingHoursInMonth() {
        return workingHoursInMonth;
    }

    public TrackingRequest setWorkingHoursInMonth(int workingHoursInMonth) {
        this.workingHoursInMonth = workingHoursInMonth;
        return this;
    }

    public int getWorkDayStartHour() {
        return workDayStartHour;
    }

    public TrackingRequest setWorkDayStartHour(int workDayStartHour) {
        this.workDayStartHour = workDayStartHour;
        return this;
    }

    public int getWorkDayEndHour() {
        return workDayEndHour;
    }

    public TrackingRequest setWorkDayEndHour(int workDayEndHour) {
        this.workDayEndHour = workDayEndHour;
        return this;
    }

    public int getSalaryPeriodStartDay() {
        return salaryPeriodStartDay;
    }

    public TrackingRequest setSalaryPeriodStartDay(int salaryPeriodStartDay) {
        this.salaryPeriodStartDay = salaryPeriodStartDay;
        return this;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public TrackingRequest setMonthlySalary(double monthlySalary) {
        this.monthlySalary = monthlySalary;
        return this;
    }
}
