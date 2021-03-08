package ee.uustal.yurozdollarzyeniz.mock;

import ee.uustal.yurozdollarzyeniz.service.http.DefaultCalendarificService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CalendarificServiceMock implements DefaultCalendarificService {

    @Override
    public List<LocalDate> getHolidays(String countryCode, int year) {

        return List.of(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 2, 24),
                LocalDate.of(year, 4, 2),
                LocalDate.of(year, 6, 23),
                LocalDate.of(year, 6, 24),
                LocalDate.of(year, 8, 20),
                LocalDate.of(year, 12, 24)
        );

    }
}
