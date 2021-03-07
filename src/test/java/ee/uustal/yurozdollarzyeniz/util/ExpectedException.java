package ee.uustal.yurozdollarzyeniz.util;

import org.junit.Assert;

public class ExpectedException {

    public static void expect(Runnable fn, Class<? extends Exception> exceptionClass) {
        try {
            fn.run();
            String exceptionClassName = exceptionClass.getName();
            Assert.fail("Expected method to fail with exception: " + exceptionClassName);
        } catch (Exception e) {
            Assert.assertEquals("Exception class mismatch", exceptionClass, e.getClass());
        }
    }
}