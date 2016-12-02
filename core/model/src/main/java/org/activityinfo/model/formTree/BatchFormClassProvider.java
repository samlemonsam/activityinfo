package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.Map;

public interface BatchFormClassProvider extends FormClassProvider {

    Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds);

}
