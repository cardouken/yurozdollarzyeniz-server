package ee.uustal.yurozdollarzyeniz;

import ee.uustal.yurozdollarzyeniz.service.TrackingService;
import ee.uustal.yurozdollarzyeniz.usecase.GetTrackingBuilder;
import ee.uustal.yurozdollarzyeniz.util.TimeProviderSpyHelper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = YurozdollarzyenizApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(BaseTest.TEST_PROFILE)
public class BaseTest {

    static final String TEST_PROFILE = "test";

    @Autowired
    private TimeProviderSpyHelper timeProviderSpyHelper;

    @Autowired
    private TrackingService trackingService;

    @BeforeEach
    public void beforeMethod() {
        timeProviderSpyHelper.reset();
    }

    public GetTrackingBuilder getTracking() {
        return new GetTrackingBuilder(trackingService);
    }
}
