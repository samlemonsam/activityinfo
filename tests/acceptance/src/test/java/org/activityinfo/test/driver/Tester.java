package org.activityinfo.test.driver;

/**
 * Created by yuriy on 6/29/2015.
 */
public class Tester {

    private Tester() {
    }

    public static void sleepSeconds(int seconds) {
        sleep(seconds * 1000);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
