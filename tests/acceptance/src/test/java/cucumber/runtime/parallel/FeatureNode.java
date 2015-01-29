package cucumber.runtime.parallel;

import com.google.common.collect.Lists;
import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Feature;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

import java.util.List;

public class FeatureNode implements Node {
    
    private final CucumberFeature cucumberFeature;
    private final Description description;
    
    private List<Node> branches = Lists.newArrayList();

    public FeatureNode(CucumberFeature cucumberFeature,
                       List<Parameter> parameters) throws InitializationError {
        
        this.cucumberFeature = cucumberFeature;
        this.description = Description.createSuiteDescription(getName(), cucumberFeature.getGherkinFeature());

        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            for(Parameter parameter : parameters) {
                try {
                    Node child = createBranch(cucumberTagStatement, parameter);
                    description.addChild(child.getDescription());
                    branches.add(child);

                } catch (InitializationError e) {
                    throw new CucumberException("Failed to create scenario node", e);
                }
            }
        }
    }

    public String getName() {
        Feature feature = cucumberFeature.getGherkinFeature();
        return feature.getKeyword() + ": " + feature.getName();
    }

    public Description getDescription() {
        return description;
    }
    

    private Node createBranch(CucumberTagStatement cucumberTagStatement, Parameter parameter) throws InitializationError {

        if (cucumberTagStatement instanceof CucumberScenario) {

            return new ExecutionUnit(
                    parameter,
                    Gherkin.parametrize(cucumberFeature, (CucumberScenario) cucumberTagStatement, parameter));

        } else {
            throw new UnsupportedOperationException("todo: ScenarioOutline");
//                        featureElementRunner = new ParametrizedScenarioOutlineRunner(parameter,
//                                (CucumberScenarioOutline) cucumberTagStatement, jUnitReporter);
        }
    }

    @Override
    public void start(Reporter reporter, Formatter formatter) {
        formatter.uri(cucumberFeature.getPath());
        formatter.feature(cucumberFeature.getGherkinFeature());
    }
    
    @Override
    public List<Node> getBranches() {
        return branches;
    }

    @Override
    public void finish(Reporter reporter, Formatter formatter) {
        formatter.eof();
    }
}
