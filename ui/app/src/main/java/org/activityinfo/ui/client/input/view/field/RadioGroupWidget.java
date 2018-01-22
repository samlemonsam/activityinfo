package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.RadioCell;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.InsertContainer;
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

    public interface Appearance {

        InsertContainer createContainer();

        RadioCell.RadioAppearance getRadioAppearance();
    }

    private static class DefaultApperance implements Appearance {

        @Override
        public InsertContainer createContainer() {
            return new FlowLayoutContainer();
        }

        @Override
        public RadioCell.RadioAppearance getRadioAppearance() {
            return GWT.create(RadioCell.RadioAppearance.class);
        }
    }

    private final InsertContainer container;
    private final ToggleGroup group;
    private final Map<ResourceId, CheckBox> radios = new HashMap<>();

    private boolean relevant = true;

    public RadioGroupWidget(EnumType type, FieldUpdater updater) {
        this(type, new DefaultApperance(), updater);
    }

    public RadioGroupWidget(EnumType type, Appearance appearance, FieldUpdater updater) {

        container = appearance.createContainer();
        group = new ToggleGroup();

        for (EnumItem enumItem : type.getValues()) {
            Radio radio = new Radio(new RadioCell(appearance.getRadioAppearance()));
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
    public void clear() {
        for (CheckBox checkBox : radios.values()) {
            checkBox.setValue(false);
        }
    }

    @Override
    public void setRelevant(boolean relevant) {
        if(this.relevant != relevant) {
            for (CheckBox checkBox : radios.values()) {
                checkBox.setEnabled(relevant);
            }
            this.relevant = relevant;
        }
    }
}
