package org.activityinfo.test.acceptance;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.api.junit.ParametrizedCucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(format = {"pretty", "html:target/cucumber-html-report", "json:target/cucumber-report.json"})
public class AcceptanceIT {



}
