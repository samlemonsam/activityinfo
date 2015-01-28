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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParametrizedExecutionUnitRunner extends ParentRunner<Step> {
    private final ParametrizedRuntime runtime;
    private final CucumberScenario cucumberScenario;
    private final ParametrizedJunitReporter jUnitReporter;
    private Description description;
    private final Map<Step, Description> stepDescriptions = new HashMap<Step, Description>();
    private final List<Step> runnerSteps = new ArrayList<Step>();

    public ParametrizedExecutionUnitRunner(ParametrizedRuntime runtime, CucumberScenario cucumberScenario, 
                                           ParametrizedJunitReporter jUnitReporter) throws InitializationError {
        super(ExecutionUnitRunner.class);
        this.runtime = runtime;
        this.cucumberScenario = cucumberScenario;
        this.jUnitReporter = jUnitReporter;
    }

    public List<Step> getRunnerSteps() {
        return runnerSteps;
    }

    @Override
    protected List<Step> getChildren() {
        return cucumberScenario.getSteps();
    }

    @Override
    public String getName() {
        return runtime.decorateName(cucumberScenario.getVisualName());
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), cucumberScenario.getGherkinModel());

            if (cucumberScenario.getCucumberBackground() != null) {
                for (Step backgroundStep : cucumberScenario.getCucumberBackground().getSteps()) {
                    // We need to make a copy of that step, so we have a unique one per scenario
                    Step copy = cloneStep(backgroundStep);
                    description.addChild(describeChild(copy));
                    runnerSteps.add(copy);
                }
            }

            for (Step step : getChildren()) {
                Step copy = cloneStep(step);
                description.addChild(describeChild(copy));
                runnerSteps.add(copy);
            }
        }
        return description;
    }

    private Step cloneStep(Step step) {
        return new Step(
                step.getComments(),
                step.getKeyword(),
                step.getName(),
                step.getLine(),
                step.getRows(),
                step.getDocString()
        );
    }

    @Override
    protected Description describeChild(Step step) {
        Description description = stepDescriptions.get(step);
        if (description == null) {
            description = Description.createTestDescription(getName(), step.getKeyword() + step.getName(), step);
            stepDescriptions.put(step, description);
        }
        return description;
    }

    @Override
    public void run(final RunNotifier notifier) {
        jUnitReporter.startExecutionUnit(this, notifier);
        // This causes runChild to never be called, which seems OK.
        cucumberScenario.run(jUnitReporter, jUnitReporter, runtime.getRuntime());
        jUnitReporter.finishExecutionUnit();
    }

    @Override
    protected void runChild(Step step, RunNotifier notifier) {
        // The way we override run(RunNotifier) causes this method to never be called.
        // Instead it happens via cucumberScenario.run(jUnitReporter, jUnitReporter, runtime);
        throw new UnsupportedOperationException();
        // cucumberScenario.runStep(step, jUnitReporter, runtime);
    }
}   
