package org.activityinfo.api.tools;

import java.util.ArrayList;
import java.util.List;

public class ApiSectionModel {
    private final String tag;
    private String title;
    private List<OperationModel> operations = new ArrayList<>();

    public ApiSectionModel(String tag, String title, List<OperationModel> operations) {
        this.tag = tag;
        this.title = title;
        for (OperationModel operation : operations) {
            if(operation.getTags().contains(tag)) {
                this.operations.add(operation);
            }
        }
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public List<OperationModel> getOperations() {
        return operations;
    }
}
