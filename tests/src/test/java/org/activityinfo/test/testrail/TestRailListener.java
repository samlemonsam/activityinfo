package org.activityinfo.test.testrail;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.HashSet;
import java.util.Set;

public class TestRailListener extends RunListener {

    private Set<Integer> failures = new HashSet<>();

    @Override
    public void testFailure(Failure failure) throws Exception {
        TestRailCase testCase = failure.getDescription().getAnnotation(TestRailCase.class);
        if(testCase != null) {
            System.out.println("C" + testCase.value() + ": failed");
            failures.add(testCase.value());
        }
    }

    @Override
    public void testFinished(Description description) throws Exception {

        TestRailCase testCase = description.getAnnotation(TestRailCase.class);
        if(testCase != null) {
            System.out.println("C" + testCase.value() + ": finished");
        }
    }

}
