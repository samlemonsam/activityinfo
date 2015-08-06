package org.activityinfo.test;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.runtime.java.guice.ScenarioScope;
import cucumber.runtime.java.guice.impl.ScenarioModule;
import cucumber.runtime.java.guice.impl.SequentialScenarioScope;
import org.activityinfo.test.webdriver.WebDriverSession;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Runs a JUnit-style test case
 */
public class JUnitUiTestCase implements TestCase {

    private final TestConditions testConditions;
    private final Method testMethod;


    public JUnitUiTestCase(Method testMethod, TestConditions testConditions) {
        this.testConditions = testConditions;
        this.testMethod = testMethod;
    }

    @Override
    public String getId() {
        return testMethod.getDeclaringClass().getSimpleName() + "." + testMethod.getName() + "-" + testConditions.getId();
    }

    @Override
    public TestResult call() throws Exception {

        List<Module> modules = Lists.newArrayList();
        modules.add(new ScenarioModule(new SequentialScenarioScope()));
        modules.addAll(testConditions.getModules());
        
        Injector injector = Guice.createInjector(modules);
        injector.getInstance(ScenarioScope.class).enterScope();

        WebDriverSession session = injector.getInstance(WebDriverSession.class);
        session.beforeTest(getId());
        
        Object testClassInstance = injector.getInstance(testMethod.getDeclaringClass());

        TestResult.Builder result = TestResult.builder(this);

        try {
            testMethod.invoke(testClassInstance);
        } catch (Throwable caught) {
            caught.printStackTrace();
            result.failed();
        } finally {
            result.output().append(TestOutputStream.drainThreadOutput());
            if(session.isRunning()) {
                session.stop();
            }
            
            injector.getInstance(ScenarioScope.class).exitScope();
        }
        
        return result.build();
    }
    
    public static List<Method> findTestMethods(Class<?> testClass) {
        List<Method> methods = Lists.newArrayList();
        for (Method method : testClass.getMethods()) {
            if(method.isAnnotationPresent(org.junit.Test.class)) {
                methods.add(method);
            }
        }
        return methods;
    }
}
