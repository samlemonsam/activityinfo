package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;


public interface FormMetadataProvider {

    FormMetadata getFormMetadata(ResourceId formId);
}
