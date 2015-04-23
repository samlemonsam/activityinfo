package org.activityinfo.ui.client.component.formdesigner.palette;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.List;

public class CheckboxTemplate implements FieldTemplate {
    @Override
    public String getLabel() {
        return I18N.CONSTANTS.checkboxes();
    }

    @Override
    public FormField createField() {
        List<EnumItem> values = Lists.newArrayList();
        values.add(new EnumItem(EnumItem.generateId(), "Option 1"));
        values.add(new EnumItem(EnumItem.generateId(), "Option 2"));
        FormField field = new FormField(ResourceId.generateFieldId(EnumType.TYPE_CLASS));
        field.setLabel("Which options apply?");
        field.setType(new EnumType(Cardinality.MULTIPLE, values));

        return field;
    }
}
