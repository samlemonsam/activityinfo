package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.Radio;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnumWidget implements FieldWidget {

    private final FlowLayoutContainer container;
    private final ToggleGroup group;
    private final Map<ResourceId, CheckBox> checkBoxes = new HashMap<>();

    private boolean relevant = true;

    public EnumWidget(EnumType type, FieldUpdater updater) {

        container = new FlowLayoutContainer();
        group = new ToggleGroup();

        for (EnumItem enumItem : type.getValues()) {
            CheckBox checkBox;
            if(type.getCardinality() == Cardinality.SINGLE) {
                checkBox = new Radio();
            } else {
                checkBox = new CheckBox();
            }
            checkBox.setBoxLabel(enumItem.getLabel());
            group.add(checkBox);
            container.add(checkBox);
            checkBoxes.put(enumItem.getId(), checkBox);
        }

        group.addValueChangeHandler(event -> updater.update(input()));
    }

    private FieldInput input() {
        Set<ResourceId> items = new HashSet<>();
        for (Map.Entry<ResourceId, CheckBox> entry : checkBoxes.entrySet()) {
            if(entry.getValue().getValue()) {
                items.add(entry.getKey());
            }
        }
        if(items.isEmpty()) {
            return FieldInput.EMPTY;
        } else {
            return new FieldInput(new EnumValue(items));
        }
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    @Override
    public void setRelevant(boolean relevant) {
        if(this.relevant != relevant) {
            for (CheckBox checkBox : checkBoxes.values()) {
                checkBox.setReadOnly(!relevant);
            }
        }
    }
}
