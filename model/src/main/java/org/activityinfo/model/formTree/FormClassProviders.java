package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;
import java.util.Map;


public class FormClassProviders {

    public static FormClassProvider fromMap(final Map<ResourceId, FormClass> map) {
        return new FormClassProvider() {
            @Override
            public FormClass getFormClass(ResourceId formId) {
                return map.get(formId);
            }
        };
    }
    
    public static FormClassProvider of(FormClass... formClasses) {
        Map<ResourceId, FormClass> map = new HashMap<>();
        for (FormClass formClass : formClasses) {
            map.put(formClass.getId(), formClass);
        }
        return fromMap(map);
    }
}
