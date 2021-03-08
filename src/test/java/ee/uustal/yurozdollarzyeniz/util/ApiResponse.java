package ee.uustal.yurozdollarzyeniz.util;

import io.restassured.path.json.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;

import java.util.List;
import java.util.Objects;

public class ApiResponse {

    private final JsonPath json;

    public ApiResponse(String json) {
        if (StringUtils.isBlank(json)) {
            this.json = JsonPath.from("{}");
        } else {
            this.json = JsonPath.from(json);
        }
    }

    public ApiResponse assertThat(String path, String expected) {
        Assert.assertEquals(expected, json.getString(path));
        return this;
    }

    public ApiResponse assertThatArraySize(String path, int expectedSize) {
        List<Object> list = Objects.requireNonNull(json.getList(path), "Could not find array at path: " + path);
        Assert.assertEquals(expectedSize, list.size());
        return this;
    }

    public ApiResponse assertExists(String path) {
        Assert.assertNotNull(json.getString(path));
        return this;
    }

    public ApiResponse assertNotExists(String path) {
        Assert.assertNull(json.getString(path));
        return this;
    }

    public ApiResponse assertThatStartsWith(String path, String expectedStartsWith) {
        String s = Objects.requireNonNull(json.getString(path), "Could not find object at path: " + path);
        Assert.assertTrue(s + " didn't start with: " + expectedStartsWith, s.startsWith(expectedStartsWith));
        return this;
    }

    public ApiResponse print() {
        LogManager.getLogger("test").info(this.json.prettify());
        return this;
    }

    public ApiResponse separator() {
        return this;
    }

}
