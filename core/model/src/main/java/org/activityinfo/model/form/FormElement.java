package org.activityinfo.model.form;

import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.ResourceId;

import java.io.Serializable;

public abstract class FormElement implements IsRecord, Serializable {

    public abstract ResourceId getId();

    public abstract String getLabel();

    public abstract Record asRecord();
}
