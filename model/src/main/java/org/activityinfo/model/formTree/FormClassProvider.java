package org.activityinfo.model.formTree;


import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

public interface FormClassProvider {

    FormClass getFormClass(ResourceId formId);
    

}
