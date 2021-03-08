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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class CalendarificService implements DefaultCalendarificService {

    //    private static final String API_KEY = System.getenv("API_KEY");
    private static final String API_KEY = "f9dcd1e9320bab5c0c17cd2545dd4d396e1f41c7";
    private static final String BASE_URI = "https://calendarific.com/api";

    private final RestTemplate calendarificRestTemplate;

    public CalendarificService(RestTemplate calendarificRestTemplate) {
        this.calendarificRestTemplate = calendarificRestTemplate;
    }

    @Override
    public List<LocalDate> getHolidays(String countryCode, int year) {
        final Map<String, Object> requestParams = Map.ofEntries(
                Map.entry("api_key", API_KEY),
                Map.entry("country", countryCode),
                Map.entry("year", year),
                Map.entry("type", "national")
        );
        final URI uri = calendarificRestTemplate.getUriTemplateHandler()
                .expand(BASE_URI + "/v2/holidays?&api_key={api_key}&country={country}&year={year}&type={type}", requestParams);
        final ResponseEntity<String> response = calendarificRestTemplate.getForEntity(uri, String.class);

        if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            throw new RuntimeException("Calendarific ratelimit exceeded! Try again later.");
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

        return parseJson(response.getBody());
    }

    private List<LocalDate> parseJson(String json) {
        final List<String> holidays = JsonPath.from(json).getList("response.holidays.date.iso", String.class);

        return holidays.stream()
                .map(LocalDate::parse)
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());
    }
}
