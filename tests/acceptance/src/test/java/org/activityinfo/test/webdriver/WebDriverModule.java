package org.activityinfo.test.webdriver;

import com.google.inject.AbstractModule;


public class WebDriverModule extends AbstractModule {
    @Override
    protected void configure() {


        if(SauceLabsDriverProvider.isEnabled()) {
            System.out.println("Using SauceLabs as WebDriver");
    //        binder.addBinding().to(SauceLabsDriverProvider.class);
            bind(WebDriverProvider.class).to(SauceLabsDriverProvider.class);
        } else {
            System.out.println("Using PhantomJS as WebDriver");
            bind(WebDriverProvider.class).to(PhantomJsProvider.class);
        }
    }
}
