package org.activityinfo.api.tools;

import java.util.Arrays;
import java.util.List;

/**
 * Root object passed to the template generator
 */
public class DocModel {

    /**
     * General topics written in markdown and read from src/main/content
     */
    private String topics;

    /**
     * The OpenAPI specification model
     */
    private SpecModel spec;

    /**
     * Programming languages for which we provide examples
     */
    private List<String> languages;

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public SpecModel getSpec() {
        return spec;
    }

    public void setSpec(SpecModel spec) {
        this.spec = spec;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(String... languages) {
        this.languages = Arrays.asList("shell", "R");
    }
}
