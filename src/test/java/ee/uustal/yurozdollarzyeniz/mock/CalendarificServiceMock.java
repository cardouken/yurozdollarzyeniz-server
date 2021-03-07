package ee.uustal.yurozdollarzyeniz.mock;

import ee.uustal.yurozdollarzyeniz.pojo.Holiday;
import ee.uustal.yurozdollarzyeniz.service.http.DefaultCalendarificService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CalendarificServiceMock implements DefaultCalendarificService {

    @Override
    public List<Holiday> getHolidays(String countryCode, int year) {

        return List.of(
                new Holiday("new years", "2021-01-01"),
                new Holiday("Independence Day", "2021-02-24"),
                new Holiday("some new stuff", "2021-04-02"),
                new Holiday("Victory Day", "2021-06-23"),
                new Holiday("no work", "2021-06-24"),
                new Holiday("more rest", "2021-08-20"),
                new Holiday("Christmas Eve", "2021-12-24")
        );

    }
}
