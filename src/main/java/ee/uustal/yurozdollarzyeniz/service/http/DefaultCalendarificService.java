package ee.uustal.yurozdollarzyeniz.service.http;

import java.time.LocalDate;
import java.util.List;

public interface DefaultCalendarificService {

    List<LocalDate> getHolidays(String countryCode, int year);

}
