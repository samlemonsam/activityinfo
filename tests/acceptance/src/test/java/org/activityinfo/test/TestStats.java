package org.activityinfo.test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Records overall test results for an acceptance test run
 */
public class TestStats {

    private int count;
    private ArrayList<String> failedTests = new ArrayList<>();
    
    private long totalRunningTime = 0;
    
    private long wallTime;
    
    private long startTime = System.currentTimeMillis();
    
    public synchronized void recordResult(String testSuite, String name, long millis, boolean passed) {
        if(!passed) {
            failedTests.add(testSuite + " / " + name);
        }
        count++;
        totalRunningTime += millis;
    }
    
    public void finished() {
        wallTime = System.currentTimeMillis() - startTime;
    }
    
    public void printSummary() {
        System.out.println();
        System.out.println(format("Finished in %d minutes, total running time was %d minutes. (Parallelism: %.1f)", 
                TimeUnit.MILLISECONDS.toMinutes(wallTime),
                TimeUnit.MILLISECONDS.toMinutes(totalRunningTime),
                (double)totalRunningTime / (double)wallTime));

        System.out.println(format("TEST RESULTS: %d Tests, %d Failed.", count, failedTests.size()));

    }
    
    public boolean hasFailures() {
        return !failedTests.isEmpty();
    }
}
