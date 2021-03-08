package ee.uustal.yurozdollarzyeniz.usecase;

import ee.uustal.yurozdollarzyeniz.controller.api.request.TrackingRequest;
import ee.uustal.yurozdollarzyeniz.controller.api.response.TrackingResponse;
import ee.uustal.yurozdollarzyeniz.service.TrackingService;
import ee.uustal.yurozdollarzyeniz.util.TestActionBuilder;

public class GetTrackingBuilder implements TestActionBuilder<TrackingResponse> {

    private final TrackingService trackingService;

    private int workDayStartHour = 10;
    private int workDayLengthInHours = 8;
    private int salaryPeriodStartDay = 28;
    private double monthlySalary = 2700;
    private String locale;
    private Integer overtimeHours;

    public GetTrackingBuilder(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    public GetTrackingBuilder workDayStartHour(int workDayStartHour) {
        this.workDayStartHour = workDayStartHour;
        return this;
    }

    public GetTrackingBuilder workingHours(int workDayLengthInHours) {
        this.workDayLengthInHours = workDayLengthInHours;
        return this;
    }

    public GetTrackingBuilder salaryDate(int salaryPeriodStartDay) {
        this.salaryPeriodStartDay = salaryPeriodStartDay;
        return this;
    }

    public GetTrackingBuilder monthlySalary(double monthlySalary) {
        this.monthlySalary = monthlySalary;
        return this;
    }

    public GetTrackingBuilder locale(String locale) {
        this.locale = locale;
        return this;
    }

    public GetTrackingBuilder overtimeHours(int overtimeHours) {
        this.overtimeHours = overtimeHours;
        return this;
    }

    @Override
    public TrackingResponse build() {
        return trackingService.track(
                new TrackingRequest()
                        .setMonthlySalary(monthlySalary)
                        .setWorkDayStartHour(workDayStartHour)
                        .setWorkDayLengthInHours(workDayLengthInHours)
                        .setSalaryDate(salaryPeriodStartDay)
                        .setLocale(locale)
                        .setOvertimeHours(overtimeHours)
        );
    }
}
