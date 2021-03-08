package ee.uustal.yurozdollarzyeniz.service.http;

import io.restassured.path.json.JsonPath;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class CalendarificService implements DefaultCalendarificService {

    private static final String API_KEY = System.getenv("API_KEY");
    private static final String BASE_URI = "https://calendarific.com/api";
    private final Map<String, List<LocalDate>> countryHolidays = new HashMap<>();

    private final RestTemplate calendarificRestTemplate;

    public CalendarificService(RestTemplate calendarificRestTemplate) {
        this.calendarificRestTemplate = calendarificRestTemplate;
    }

    @Override
    public List<LocalDate> getHolidays(String countryCode, int year) {
        if (countryHolidays.containsKey(countryCode) && countryHolidays.get(countryCode).stream().anyMatch(d -> Objects.equals(d.getYear(), year))) {
            return countryHolidays.get(countryCode);
        }

        final Map<String, Object> requestParams = Map.ofEntries(
                Map.entry("api_key", API_KEY),
                Map.entry("country", countryCode),
                Map.entry("year", year),
                Map.entry("type", "national")
        );
        final URI uri = calendarificRestTemplate.getUriTemplateHandler()
                .expand(BASE_URI + "/v2/holidays?&api_key={api_key}&country={country}&year={year}&type={type}", requestParams);
        final ResponseEntity<String> response = calendarificRestTemplate.getForEntity(uri, String.class);

        if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || response.getStatusCode() == HttpStatus.UPGRADE_REQUIRED) {
            throw new RuntimeException("Calendarific request limit exceeded! Try again later.");
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                    MessageFormat.format(
                            "Unsuccessful response: {0} received from Calendarific at endpoint {1} with response: {2}",
                            response.getStatusCodeValue(),
                            uri,
                            response.getBody()
                    )
            );
        }

        final List<LocalDate> weekDayHolidays = parseJson(response.getBody());
        countryHolidays.computeIfAbsent(countryCode, d -> new ArrayList<>(weekDayHolidays));
        return weekDayHolidays;
    }

    private List<LocalDate> parseJson(String json) {
        final List<String> holidays = JsonPath.from(json).getList("response.holidays.date.iso", String.class);

        return holidays.stream()
                .map(LocalDate::parse)
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());
    }
}
