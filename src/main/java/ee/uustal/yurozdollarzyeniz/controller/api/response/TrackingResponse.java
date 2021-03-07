package ee.uustal.yurozdollarzyeniz.controller.api.response;

import java.math.BigDecimal;

public class TrackingResponse {

    private BigDecimal earned;
    private BigDecimal earnedToday;

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
}
