package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.Input;
import com.google.appengine.tools.mapreduce.InputReader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class RecordBatchInput extends Input<RecordBatch> {

    private String formId;
    private List<String> recordIds;

    private int batchSize;

    public RecordBatchInput(String formId, List<String> recordIds, int batchSize) {
        this.formId = formId;
        this.recordIds = recordIds;
        this.batchSize = batchSize;
    }

    @Override
    public List<? extends InputReader<RecordBatch>> createReaders() throws IOException {
        return Collections.singletonList(new RecordBatchReader(formId, recordIds, batchSize));
    }

}
