package org.activityinfo.model.form;

import com.google.common.collect.Lists;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public abstract class FormElement {

    public abstract ResourceId getId();

    public abstract String getLabel();

    public abstract Record asRecord();

    public static List<Record> asRecordList(List<FormElement> elements) {
        List<Record> records = Lists.newArrayList();
        for(FormElement element : elements) {
            records.add(element.asRecord());
        }
        return records;
    }

}
