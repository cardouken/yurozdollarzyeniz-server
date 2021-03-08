package ee.uustal.yurozdollarzyeniz.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class JsonUtility {

    private static final String DATETIME_JSON_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final ObjectMapper objectMapper;

    private static class BigDecimalModule extends SimpleModule {
        public BigDecimalModule() {
            super("BigDecimal serialization module");
            addSerializer(new PriceSerializer(BigDecimal.class));
        }
    }

    private static class PriceSerializer extends StdSerializer<BigDecimal> {
        public PriceSerializer(Class<BigDecimal> t) {
            super(t);
        }

        @Override
        public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeNumber(value.setScale(2, RoundingMode.HALF_UP));
        }
    }

    static {
        Jdk8Module jdk8Module = new Jdk8Module();
        BigDecimalModule bigDecimalModule = new BigDecimalModule();

        ZoneId zoneId = ZoneId.of("UTC");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATETIME_JSON_FORMAT_PATTERN);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(zoneId));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_JSON_FORMAT_PATTERN).withZone(zoneId);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(jdk8Module);
        objectMapper.registerModule(javaTimeModule);
        objectMapper.registerModule(bigDecimalModule);
        objectMapper.setDateFormat(simpleDateFormat);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
