package org.activityinfo.test.testrail;

import org.codehaus.jackson.annotate.JsonProperty;

public class TestResult {

    @JsonProperty("case_id")
    private int caseId;

    @JsonProperty("status_id")
    private int statusId;

    private String comment;

    private String version;

}
