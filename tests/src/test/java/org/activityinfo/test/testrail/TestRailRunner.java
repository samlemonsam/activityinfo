package org.activityinfo.test.testrail;

import org.activityinfo.test.ui.UiTestSuite;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;

/**
 * Runs a test suite and reports the result to TestRail
 */
public class TestRailRunner {


    public static void main(String[] args) {

        JUnitCore jUnit = new JUnitCore();
        jUnit.addListener(new TestRailListener());
        jUnit.run(Computer.serial(), UiTestSuite.class);


    }


}
