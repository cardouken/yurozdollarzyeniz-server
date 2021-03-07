package ee.uustal.yurozdollarzyeniz.service.http;

import ee.uustal.yurozdollarzyeniz.pojo.Holiday;

import java.util.List;

public interface DefaultCalendarificService {

    List<Holiday> getHolidays(String countryCode, int year);

}
