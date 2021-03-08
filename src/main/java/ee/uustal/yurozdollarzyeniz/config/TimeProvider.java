package ee.uustal.yurozdollarzyeniz.config;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class TimeProvider {

    public LocalDateTime dateTimeNow() {
        return LocalDateTime.now();
    }

    public LocalDate dateNow() {
        return LocalDate.now();
    }

}