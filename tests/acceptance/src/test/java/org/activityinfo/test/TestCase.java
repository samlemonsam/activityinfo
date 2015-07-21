package org.activityinfo.test;

import java.util.concurrent.Callable;


public interface TestCase extends Callable<TestResult> {
    
    String getId();
}
