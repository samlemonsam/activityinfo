package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;

import java.util.List;
import java.util.Map;

public interface Ids {

    int databaseId();

    ResourceId formId(String defaultName);

    ResourceId enumFieldId(String defaultName);

    ResourceId fieldId(String defaultName);

    ResourceId recordId(ResourceId formId, int index);

    EnumItem enumItem(String label);

    List<FormField> builtinFields();

    Map<ResourceId, FieldValue> builtinValues();
}
