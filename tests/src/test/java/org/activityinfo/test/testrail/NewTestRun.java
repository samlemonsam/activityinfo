package org.activityinfo.test.testrail;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class NewTestRun {


    /**
     * The name of the test run
     */
    private String name;

    private String description;

    @JsonProperty("case_ids")
    private List<Integer> caseIds;

    public NewTestRun(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCaseIds(List<Integer> caseIds) {
        this.caseIds = caseIds;
    }

    public List<Integer> getCaseIds() {
        return caseIds;
    }
}
