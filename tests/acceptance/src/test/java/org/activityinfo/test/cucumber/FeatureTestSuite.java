package org.activityinfo.test.cucumber;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.runtime.*;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.guice.ScenarioScope;
import cucumber.runtime.java.guice.impl.ScenarioModule;
import cucumber.runtime.java.guice.impl.SequentialScenarioScope;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import org.activityinfo.test.TestLogger;
import org.activityinfo.test.TestReporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Runs a single Cucumber .feature file. Each Scenario or Scenario outline is 
 * considered a "test
 */
public class FeatureTestSuite implements Runnable {

    private final Injector injector;
    private final CucumberFeature feature;
    private final RuntimeOptions options;
    private final TestReporter reporter;
    private final cucumber.runtime.Runtime runtime;
    private ReportingAdapter adapter;

    public FeatureTestSuite(RuntimeOptions options, CucumberFeature feature, TestReporter reporter, Module... modules) {
        this.feature = feature;
        this.options = options;
        this.reporter = reporter;

        List<Module> moduleList = new ArrayList<>();
        moduleList.add(new ScenarioModule(new SequentialScenarioScope()));
        moduleList.addAll(Arrays.asList(modules));
        moduleList.add(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(TestLogger.class).toInstance(new TestLogger() {

                    @Override
                    public void info(String message) {
                        if(adapter != null) {
                            adapter.write(message);
                        }
                    }
                });
            }
        });
        
        this.injector = Guice.createInjector(moduleList);

        ClassLoader classLoader = getClass().getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        JavaBackend backend = new JavaBackend(new GuiceObjectFactory(injector), classFinder);
        runtime = new cucumber.runtime.Runtime(resourceLoader, classLoader, Collections.singleton(backend), options);
    }

    @Override
    public void run() {

        reporter.testSuiteStarted(feature.getGherkinFeature().getName());
        
        for (CucumberTagStatement element : feature.getFeatureElements()) {
            runScenario(element);
        }
        
        reporter.testSuiteFinished();
    }

    private void runScenario(CucumberTagStatement element) {
        
        String testName = element.getGherkinModel().getName();

        reporter.testStarted(testName);

        adapter = new ReportingAdapter();
        adapter.uri(feature.getPath());
        adapter.feature(feature.getGherkinFeature());

        element.run(adapter, adapter, runtime);
        adapter.eof();
        adapter.done();
        adapter.close();

        reporter.testFinished(testName, adapter.isPassed(), adapter.getOutput());
        

    }

    private static class GuiceObjectFactory implements ObjectFactory {

        private final Injector injector;

        private GuiceObjectFactory(Injector injector) {
            this.injector = injector;
        }

        @Override
        public void addClass(Class<?> clazz) {}

        @Override
        public void start() {
            injector.getInstance(ScenarioScope.class).enterScope();
        }

        @Override
        public void stop() {
            injector.getInstance(ScenarioScope.class).exitScope();
        }

        @Override
        public <T> T getInstance(Class<T> clazz) {
            return injector.getInstance(clazz);
        }

    }
}
