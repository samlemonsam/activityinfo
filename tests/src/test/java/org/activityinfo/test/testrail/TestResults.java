package org.activityinfo.test.testrail;

import java.util.ArrayList;
import java.util.List;

public class TestResults {
    private final List<TestResult> results = new ArrayList<>();

    public List<TestResult> getResults() {
        return results;
    }

    public void add(TestResult result) {
        results.add(result);
    }
}
