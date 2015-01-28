package cucumber.runtime.junit;

import cucumber.api.Profile;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParametrizedFeatureRunner extends ParentRunner<ParentRunner> {
    private final List<ParentRunner> children = new ArrayList<ParentRunner>();

    private final CucumberFeature cucumberFeature;
    private final JUnitReporter jUnitReporter;
    private List<ParametrizedRuntime> parameters;
    private Map<Profile, Runtime> profiles;
    private Description description;

    public ParametrizedFeatureRunner(CucumberFeature cucumberFeature, JUnitReporter jUnitReporter,
                                     List<ParametrizedRuntime> parameters) throws InitializationError {
        super(null);
        this.cucumberFeature = cucumberFeature;
        this.jUnitReporter = jUnitReporter;
        this.parameters = parameters;
        buildFeatureElementRunners();
    }

    @Override
    public String getName() {
        Feature feature = cucumberFeature.getGherkinFeature();
        return feature.getKeyword() + ": " + feature.getName();
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), cucumberFeature.getGherkinFeature());
            for (ParentRunner child : getChildren()) {
                description.addChild(describeChild(child));
            }
        }
        return description;
    }

    @Override
    protected List<ParentRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(ParentRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(ParentRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    public void run(RunNotifier notifier) {
        jUnitReporter.uri(cucumberFeature.getPath());
        jUnitReporter.feature(cucumberFeature.getGherkinFeature());
        super.run(notifier);
        jUnitReporter.eof();
    }

    private void buildFeatureElementRunners() {


        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            for(ParametrizedRuntime parameter : parameters) {
                try {
                    ParentRunner featureElementRunner;
                    if (cucumberTagStatement instanceof CucumberScenario) {
                        featureElementRunner = new ParametrizedExecutionUnitRunner(parameter,
                                clone((CucumberScenario) cucumberTagStatement), jUnitReporter);
                    } else {
                        featureElementRunner = new ParametrizedScenarioOutlineRunner(parameter,
                                (CucumberScenarioOutline) cucumberTagStatement, jUnitReporter);
                    }
                    children.add(featureElementRunner);
                } catch (InitializationError e) {
                    throw new CucumberException("Failed to create scenario runner", e);
                }
            }
        }
    }

    private CucumberScenario clone(CucumberScenario scenario) {
        Scenario gherkinScenario = (Scenario) scenario.getGherkinModel();
        CucumberScenario copy = new CucumberScenario(cucumberFeature, scenario.getCucumberBackground(), gherkinScenario);
        for (Step step : scenario.getSteps()) {
            copy.step(copyStep(step));
        }
        return copy;
    }

    private Step copyStep(Step backgroundStep) {
        return new Step(
                backgroundStep.getComments(),
                backgroundStep.getKeyword(),
                backgroundStep.getName(),
                backgroundStep.getLine(),
                backgroundStep.getRows(),
                backgroundStep.getDocString()
        );
    }
}
