package ee.uustal.yurozdollarzyeniz.util;

import ee.uustal.yurozdollarzyeniz.config.JsonUtility;

public interface TestActionBuilder<T> {

    T build();

    default ApiResponse buildApi() {
        T build = build();
        return new ApiResponse(JsonUtility.toJson(build));
    }

    default Void empty() {
        return null;
    }
}
