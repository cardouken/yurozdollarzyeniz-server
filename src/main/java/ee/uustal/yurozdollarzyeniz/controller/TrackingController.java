package ee.uustal.yurozdollarzyeniz.controller;

import ee.uustal.yurozdollarzyeniz.controller.api.request.TrackingRequest;
import ee.uustal.yurozdollarzyeniz.controller.api.response.TrackingResponse;
import ee.uustal.yurozdollarzyeniz.service.TrackingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping(value = "/track")
    public TrackingResponse track(@Valid TrackingRequest request) {
        return trackingService.track(request);
    }

}
