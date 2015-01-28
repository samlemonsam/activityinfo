package cucumber.runtime.junit;

import com.google.common.collect.Lists;
import cucumber.api.Profile;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;


public class ParametrizedExecutionUnitRunner extends ParentRunner<Step> {
    private Profile profile;
    private ExecutionUnitRunner delegate;
    private Description description;
    private List<Step> children = Lists.newArrayList();

    public ParametrizedExecutionUnitRunner(ParametrizedRuntime parameter, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter) throws InitializationError {
        super(ExecutionUnitRunner.class);
        this.profile = parameter.getProfile();
        this.delegate = new ExecutionUnitRunner(parameter.getRuntime(), cucumberScenario, jUnitReporter);
    }

    @Override
    public String getName() {
        return delegate.getName() + " [" + profile.getName() + "]";
    }

    @Override
    protected List<Step> getChildren() {
        return delegate.getChildren();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            Description delegateDescription = delegate.getDescription();
            description = Description.createSuiteDescription(getName(),
                    new UniqueId(profile.getId(), delegateDescription));

            for (Description child : description.getChildren()) {
                description.addChild(child);
            }
        }
        return description;
    }


    @Override
    protected Description describeChild(Step step) {
        return delegate.describeChild(step);
    }

    @Override
    public void run(final RunNotifier notifier) {
        delegate.run(notifier);
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        delegate.runChild(step, notifier);   
    }
}   
