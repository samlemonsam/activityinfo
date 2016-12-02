package org.activityinfo.api.tools;

import java.util.Arrays;
import java.util.List;

/**
 * Root object passed to the template generator
 */
public class DocModel {

    private String topics;
    private SpecModel spec;
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
