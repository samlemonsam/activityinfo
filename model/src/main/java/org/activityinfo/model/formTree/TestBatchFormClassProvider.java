package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestBatchFormClassProvider implements BatchFormClassProvider {

    private final Map<ResourceId, FormClass> map = new HashMap<>();

    @Override
    public FormClass getFormClass(ResourceId formId) {
        FormClass formClass = map.get(formId);
        if(formClass == null) {
            throw new IllegalArgumentException("No such form: " + formId);
        }
        return formClass;
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        Map<ResourceId, FormClass> resultMap = new HashMap<>();
        for (ResourceId formId : formIds) {
            FormClass formClass = map.get(formId);
            if(formClass != null) {
                resultMap.put(formId, formClass);
            }
        }
        return resultMap;
    }

    public void add(FormClass formClass) {
        map.put(formClass.getId(), formClass);
    }

    public void addAll(List<FormClass> formClasses) {
        for (FormClass formClass : formClasses) {
            add(formClass);
        }
    }
}
