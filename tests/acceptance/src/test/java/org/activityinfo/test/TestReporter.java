package org.activityinfo.test;


import java.util.List;

public interface TestReporter {
    
    void testSuiteStarted(String suiteName);
    
    void testStarted(String name);

    void testFinished(String name, boolean passed, String output);
    
    void testSuiteFinished();
}
