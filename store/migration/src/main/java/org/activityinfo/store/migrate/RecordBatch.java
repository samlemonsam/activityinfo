package org.activityinfo.store.migrate;

import java.io.Serializable;
import java.util.List;

public class RecordBatch implements Serializable {
    private String formId;
    private List<String> records;

    public RecordBatch(String formId, List<String> records) {
        this.formId = formId;
        this.records = records;
    }

    public String getFormId() {
        return formId;
    }

    public List<String> getRecords() {
        return records;
    }


}
