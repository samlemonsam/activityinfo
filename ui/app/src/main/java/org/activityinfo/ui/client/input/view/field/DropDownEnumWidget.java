package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays a single-select enum field as a ComboBox
 */
public class DropDownEnumWidget implements FieldWidget {

    private ComboBox<EnumItem> comboBox;
    private final ListStore<EnumItem> store;
    private final Map<ResourceId, String> labels = new HashMap<>();

    public DropDownEnumWidget(EnumType type, FieldUpdater updater) {

        for (EnumItem enumItem : type.getValues()) {
            labels.put(enumItem.getId(), enumItem.getLabel());
        }

        store = new ListStore<>(item -> item.getId().asString());
        store.addAll(type.getValues());

        comboBox = new ComboBox<>(store, item -> item.getLabel());
        comboBox.setForceSelection(true);
        comboBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        comboBox.addSelectionHandler(event -> {
           if(event.getSelectedItem() == null) {
               updater.update(FieldInput.EMPTY);
           } else {
               updater.update(new FieldInput(new EnumValue(event.getSelectedItem().getId())));
           }
        });
    }

    @Override
    public void init(FieldValue value) {

        EnumValue enumValue = (EnumValue) value;
        String label = labels.get(enumValue.getValueId());

        comboBox.setText(label);
    }

    @Override
    public void setRelevant(boolean relevant) {
        comboBox.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return comboBox;
    }
}
