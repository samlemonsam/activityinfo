package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

/**
 * Created by alex on 30-1-17.
 */
public interface TestForm {

    public ResourceId getFormId();

    public FormClass getFormClass();

}
