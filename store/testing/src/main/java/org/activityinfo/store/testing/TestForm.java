package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;


public interface TestForm {

    ResourceId getFormId();

    FormClass getFormClass();

    List<FormInstance> getRecords();

}
