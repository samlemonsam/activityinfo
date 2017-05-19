package org.activityinfo.ui.client.component.form.field;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.promise.Promise;

public class EnumFieldWidget implements FormFieldWidget<EnumValue> {

    private final FormFieldWidget<EnumValue> widget;

    public EnumFieldWidget(EnumType enumType, final ValueUpdater<EnumValue> valueUpdater, FieldWidgetMode fieldWidgetMode) {
        widget = createWidget(enumType, valueUpdater, fieldWidgetMode);
    }

    private FormFieldWidget<EnumValue> createWidget(EnumType enumType, final ValueUpdater<EnumValue> valueUpdater, FieldWidgetMode fieldWidgetMode) {
        EnumType.Presentation presentation = EnumType.Presentation.RADIO_BUTTON;
        if(fieldWidgetMode == FieldWidgetMode.NORMAL) {
            // Only apply presentation choices in data entry mode
            presentation = enumType.getEffectivePresentation();
        } else {
            presentation = EnumType.Presentation.RADIO_BUTTON;
        }

        if(presentation == EnumType.Presentation.RADIO_BUTTON) {
            return new EnumCheckboxWidget(enumType, valueUpdater, fieldWidgetMode);
        } else {
            return new EnumDropDownWidget(enumType, valueUpdater);
        }
    }

    @Override
    public void fireValueChanged() {
        widget.fireValueChanged();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        widget.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return widget.isReadOnly();
    }

    @Override
    public Promise<Void> setValue(EnumValue value) {
        return widget.setValue(value);
    }

    @Override
    public void clearValue() {
        widget.clearValue();
    }

    @Override
    public void setType(FieldType type) {
        widget.setType(type);
    }

    @Override
    public boolean isValid() {
        return widget.isValid();
    }

    @Override
    public Widget asWidget() {
        return widget.asWidget();
    }
}
