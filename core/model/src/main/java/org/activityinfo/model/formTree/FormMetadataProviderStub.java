package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;
import java.util.Map;

public class FormMetadataProviderStub implements FormMetadataProvider {

    private Map<ResourceId, FormClass> map = new HashMap<>();

    public FormMetadataProviderStub(FormClass... forms) {
        for (FormClass form : forms) {
            map.put(form.getId(), form);
        }
    }

    @Override
    public FormMetadata getFormMetadata(ResourceId formId) {
        if(map.containsKey(formId)) {
            return FormMetadata.of(1, map.get(formId), FormPermissions.full());
        } else {
            return FormMetadata.notFound(formId);
        }
    }
}
