package org.activityinfo.test.acceptance;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.ParametrizedCucumber;
import org.junit.runner.RunWith;

@RunWith(ParametrizedCucumber.class)
@CucumberOptions(format = {"pretty", "html:target/cucumber-html-report", "json:target/cucumber-report.json"})
public class AcceptanceIT {



}
