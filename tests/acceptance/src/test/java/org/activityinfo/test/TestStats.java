package org.activityinfo.test;

import java.util.ArrayList;

import static java.lang.String.format;

/**
 * Records overall test results for an acceptance test run
 */
public class TestStats {

    private int count;
    private ArrayList<String> failedTests = new ArrayList<>();
    
    public synchronized void recordResult(String testSuite, String name, boolean passed) {
        if(!passed) {
            failedTests.add(testSuite + " / " + name);
        }
        count++;
    }
    
    public void printSummary() {
        System.out.println();
        System.out.println(format("TEST RESULTS: %d Tests, %d Failed.", count, failedTests.size()));
    }
}
