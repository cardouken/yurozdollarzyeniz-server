package ee.uustal.yurozdollarzyeniz.usecase;

import ee.uustal.yurozdollarzyeniz.controller.api.request.TrackingRequest;
import ee.uustal.yurozdollarzyeniz.controller.api.response.TrackingResponse;
import ee.uustal.yurozdollarzyeniz.service.TrackingService;
import ee.uustal.yurozdollarzyeniz.util.TestActionBuilder;

public class GetTrackingBuilder implements TestActionBuilder<TrackingResponse> {

    private final TrackingService trackingService;

    private int workingHoursInMonth = 184;
    private int workDayStartHour = 10;
    private int workDayLengthInHours = 8;
    private int salaryPeriodStartDay = 28;
    private double monthlySalary = 2700;

    public GetTrackingBuilder(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    public GetTrackingBuilder workingHoursInMonth(int workingHoursInMonth) {
        this.workingHoursInMonth = workingHoursInMonth;
        return this;
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

    @Override
    public TrackingResponse build() {
        return trackingService.track(
                new TrackingRequest()
                        .setMonthlySalary(monthlySalary)
                        .setWorkDayStartHour(workDayStartHour)
                        .setWorkDayLengthInHours(workDayLengthInHours)
                        .setWorkingHoursInMonth(workingHoursInMonth)
                        .setSalaryDate(salaryPeriodStartDay)
        );
    }
}
