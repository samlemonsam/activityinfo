package org.activityinfo.test;


public interface TestReporter {
    
    void testSuiteStarted(String suiteName);
    
    void testStarted(String name);

    void testFinished(String name, boolean passed, String output);
    
    void testSuiteFinished();

    void attach(String mimeType, byte[] data);
}
