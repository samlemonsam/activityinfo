package org.activityinfo.test.webdriver;

import com.google.inject.Inject;
import com.saucelabs.common.Utils;
import com.saucelabs.saucerest.SauceREST;
import cucumber.api.Scenario;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

@ScenarioScoped
public class SauceReporter {

    private Provider<SauceLabsDriverProvider> provider;
    private WebDriverSession session;


    @Inject
    public SauceReporter(Provider<SauceLabsDriverProvider> provider, WebDriverSession session) {
        this.provider = provider;
        this.session = session;
    }

    public void finished(Scenario scenario) {


        SauceLabsDriverProvider sauce;
        try {
            sauce = provider.get();
        } catch(Exception e) {
            sauce = null;
        }
        if(sauce != null) {
            RemoteWebDriver driver = (RemoteWebDriver) session.getDriver();
            String sessionId = driver.getSessionId().toString();

            SauceREST sauceClient = sauce.getRestClient();
            Map<String, Object> updates = new HashMap<>();
            updates.put("passed", !scenario.isFailed());
            updates.put("name", scenario.getName());

            Utils.addBuildNumberToUpdate(updates);

            sauceClient.updateJobInfo(sessionId, updates);
        }
    }
}
