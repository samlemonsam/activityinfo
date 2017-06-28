package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.Radio;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.HashMap;
import java.util.Map;

/**
 * FieldWidget for a {@link EnumType} field that displays options as a group
 * of radio buttons.
 */
public class RadioGroupWidget implements FieldWidget {

    private final FlowLayoutContainer container;
    private final ToggleGroup group;
    private final Map<ResourceId, CheckBox> radios = new HashMap<>();

    private boolean relevant = true;

    public RadioGroupWidget(EnumType type, FieldUpdater updater) {

        container = new FlowLayoutContainer();
        group = new ToggleGroup();

        for (EnumItem enumItem : type.getValues()) {
            Radio radio = new Radio();
            radio.setBoxLabel(enumItem.getLabel());

            group.add(radio);
            container.add(radio);
            radios.put(enumItem.getId(), radio);
        }

        group.addValueChangeHandler(event -> updater.update(input()));
    }

    private FieldInput input() {
        for (Map.Entry<ResourceId, CheckBox> entry : radios.entrySet()) {
            if(entry.getValue().getValue()) {
                return new FieldInput(new EnumValue(entry.getKey()));
            }
        }
        return FieldInput.EMPTY;
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    @Override
    public void init(FieldValue value) {
        EnumValue enumValue = (EnumValue) value;
        CheckBox checkBox = radios.get(enumValue.getValueId());
        checkBox.setValue(true);
    }

    @Override
    public void setRelevant(boolean relevant) {
        if(this.relevant != relevant) {
            for (CheckBox checkBox : radios.values()) {
                checkBox.setEnabled(!relevant);
            }
        }
    }
}
