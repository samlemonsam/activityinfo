package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

class DesignIconProvider implements ModelIconProvider<ModelData> {
    @Override
    public AbstractImagePrototype getIcon(ModelData model) {
        if (model instanceof IsActivityDTO) {
            IsActivityDTO activity = (IsActivityDTO) model;
            if (activity.getClassicView()) {
                return IconImageBundle.ICONS.activity();
            } else {
                return IconImageBundle.ICONS.form();
            }
        } else if (model instanceof FieldGroup || model instanceof FolderDTO) {
            return GXT.IMAGES.tree_folder_closed();

        } else if (model instanceof AttributeGroupDTO) {
            return IconImageBundle.ICONS.attributeGroup();

        } else if (model instanceof AttributeDTO) {
            return IconImageBundle.ICONS.attribute();

        } else if (model instanceof IndicatorDTO) {
            return IconImageBundle.ICONS.indicator();

        } else if (model instanceof LocationTypeDTO) {
            return IconImageBundle.ICONS.marker();
        } else {
            return null;
        }
    }
}
