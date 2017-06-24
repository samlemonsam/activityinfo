package org.activityinfo.store.testing;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;

public interface Ids {

    int databaseId();

    ResourceId formId(String defaultName);

    ResourceId enumFieldId(String defaultName);

    ResourceId fieldId(String defaultName);

    ResourceId recordId(ResourceId formId, int index);

    EnumItem enumItem(String label);


}
