package org.activityinfo.test.harness;

import com.google.inject.AbstractModule;


public class HarnessModule extends AbstractModule {


    @Override
    protected void configure() {
//        install(new PhantomJs());
//        install(new LocalDevServer());

        install(new SauceLabs());
        install(new LiveServer());
    }
}
