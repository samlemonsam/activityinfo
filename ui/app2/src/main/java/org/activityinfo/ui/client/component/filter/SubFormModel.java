package org.activityinfo.ui.client.component.filter;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.model.ProvidesKey;
import org.activityinfo.model.resource.ResourceId;

/**
 * Entry for a SubForm in the {@link IndicatorTreePanel}
 */
class SubFormModel extends BaseModelData implements ProvidesKey {

    private ResourceId subFormId;
    
    public void setName(String name) {
        set("name", name);
    }

    public ResourceId getSubFormId() {
        return subFormId;
    }

    public void setSubFormId(ResourceId subFormId) {
        this.subFormId = subFormId;
    }

    @Override
    public String getKey() {
        return subFormId.asString();
    }
}
