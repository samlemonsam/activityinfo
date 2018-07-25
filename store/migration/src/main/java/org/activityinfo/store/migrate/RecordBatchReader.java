package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.InputReader;

import java.util.List;
import java.util.NoSuchElementException;

class RecordBatchReader extends InputReader<RecordBatch> {

    private final String formId;
    private final List<String> recordIds;

    private int currentIndex = 0;
    private int batchSize;

    public RecordBatchReader(String formId, List<String> recordIds, int batchSize) {
        this.formId = formId;
        this.recordIds = recordIds;
        this.batchSize = batchSize;
    }

    @Override
    public RecordBatch next() throws NoSuchElementException {
        if(currentIndex < recordIds.size()) {
            int fromIndex = this.currentIndex;
            int toIndex = Math.min(recordIds.size(), this.currentIndex + batchSize);

            currentIndex = toIndex;

            return new RecordBatch(formId, recordIds.subList(fromIndex, toIndex));
        }

        throw new NoSuchElementException("currentIndex = " + currentIndex + ", size = " + recordIds);
    }
}
