package ee.uustal.yurozdollarzyeniz.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Holiday {

    private String name;
    private DateWrapper date;

    public Holiday() {
    }

    public Holiday(String name, String date) {
        this.name = name;
        this.date = new DateWrapper(date);
    }

    public String getName() {
        return name;
    }

    public Holiday setName(String name) {
        this.name = name;
        return this;
    }

    public LocalDateTime getDate() {
        return LocalDateTime.of(LocalDate.parse(date.getIsoDate()), LocalTime.of(0, 0));
    }

    public Holiday setDate(DateWrapper date) {
        this.date = date;
        return this;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DateWrapper {

        public DateWrapper() {
        }

        public DateWrapper(String isoDate) {
            this.isoDate = isoDate;
        }

        @JsonProperty("iso")
        private String isoDate;

        public String getIsoDate() {
            return isoDate;
        }

        public DateWrapper setIsoDate(String isoDate) {
            this.isoDate = isoDate;
            return this;
        }
    }
}
