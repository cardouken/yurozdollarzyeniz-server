package ee.uustal.yurozdollarzyeniz.util;

import ee.uustal.yurozdollarzyeniz.config.TimeProvider;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TimeProviderSpyHelper {

    @SpyBean
    @Autowired
    private TimeProvider timeProvider;

    public void tamperTime(LocalDateTime time) {
        Mockito.doReturn(time).when(timeProvider).now();
    }

    public void reset() {
        Mockito.reset(timeProvider);
    }

}
