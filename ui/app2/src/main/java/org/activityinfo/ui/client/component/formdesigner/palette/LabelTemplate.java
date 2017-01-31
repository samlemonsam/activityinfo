package org.activityinfo.ui.client.component.formdesigner.palette;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormLabel;
import org.activityinfo.model.resource.ResourceId;

/**
 * Created by yuriyz on 4/15/2016.
 */
public class LabelTemplate implements Template<FormLabel> {

    @Override
    public String getLabel() {
        return I18N.CONSTANTS.label();
    }

    @Override
    public FormLabel create() {
        return new FormLabel(ResourceId.generateId(), getLabel());
    }
}
