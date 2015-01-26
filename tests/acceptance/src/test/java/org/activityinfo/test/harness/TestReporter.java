package org.activityinfo.test.harness;

import cucumber.api.Scenario;


public interface TestReporter {

    void testStarting();

    public void testFinished(Scenario scenario);
}
