package org.activityinfo.test.pageobject.odk;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides the URL of the Appium Server that allows us to use WebDriver to
 * control an Android Device running ODK.
 */
public interface OdkAppiumProvider {

    URL getAppiumServerUrl() throws MalformedURLException;

}
