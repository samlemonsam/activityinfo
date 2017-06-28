package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormInstance;

import java.util.ArrayList;
import java.util.List;

public class LazyRecordList {
    private final RecordGenerator generator;
    private int count;

    private List<FormInstance> records;

    public LazyRecordList(RecordGenerator generator, int count) {
        this.generator = generator;
        this.count = count;
    }

    public List<FormInstance> get() {
        if(records == null) {
            records = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                records.add(generator.get());
            }
        }
        return records;
    }
}
