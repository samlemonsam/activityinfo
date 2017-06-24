package org.activityinfo.test.harness;

import org.activityinfo.client.ActivityInfoClient;

public class TestHarness {

    private final ActivityInfoClient client;

    public TestHarness() {
        client = new ActivityInfoClient("http://localhost:8080/", "akbertram@gmail.com", "dfdf");
    }

}
