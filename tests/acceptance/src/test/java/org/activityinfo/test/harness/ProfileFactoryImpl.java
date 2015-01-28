package org.activityinfo.test.harness;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.api.Profile;
import cucumber.runtime.ProfileFactory;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.guice.ScenarioScope;
import cucumber.runtime.java.guice.impl.ScenarioModule;
import org.activityinfo.test.sut.SystemUnderTest;
import org.activityinfo.test.webdriver.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ProfileFactoryImpl implements ProfileFactory {
    
    private final Injector injector;
    
    public ProfileFactoryImpl() {
        this.injector = Guice.createInjector(
                new SystemUnderTest(), 
                new WebDriverModule());
    }
    
    @Override
    public List<Profile> getProfiles() {
        return Arrays.<Profile>asList(
                new BrowserProfile(OperatingSystem.LINUX, BrowserVendor.CHROME, "31"),
                new BrowserProfile(OperatingSystem.MOUNTAIN_LION, BrowserVendor.SAFARI, "11"));
        
        //return Collections.<Profile>singletonList(new BrowserProfile(OperatingSystem.LINUX, BrowserVendor.CHROME, "31"));
    }

    @Override
    public Optional<ObjectFactory> createObjectFactory(Profile profile) {
        ThreadSafeScenarioScope scenarioScope = new ThreadSafeScenarioScope();
        
        List<Module> modules = Lists.newArrayList();
        modules.add(new ScenarioModule(scenarioScope));
        
        if(profile instanceof DeviceProfile) {
            DeviceProfile device = (DeviceProfile) profile;
            WebDriverProvider webDriverProvider = injector.getInstance(WebDriverProvider.class);
            if(webDriverProvider.supports(device)) {
                modules.add(new WebDriverProfileModule(webDriverProvider, (DeviceProfile) profile));
            }
        }
        
        return Optional.<ObjectFactory>of(new ProfileObjectFactory(injector.createChildInjector(modules)));
    }

    private static class ProfileObjectFactory implements ObjectFactory {

        private Injector childInjector;

        public ProfileObjectFactory(Injector childInjector) {
            this.childInjector = childInjector;
        }

        @Override
        public void start() {
            childInjector.getInstance(ScenarioScope.class).enterScope();
        }

        @Override
        public void stop() {
            childInjector.getInstance(ScenarioScope.class).exitScope();
        }

        @Override
        public void addClass(Class<?> glueClass) { }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return childInjector.getInstance(glueClass);
        }
    }
}
