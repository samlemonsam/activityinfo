package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

public interface AsyncFormClassProvider {

    Promise<FormClass> getFormClass(ResourceId formClassId);

}
