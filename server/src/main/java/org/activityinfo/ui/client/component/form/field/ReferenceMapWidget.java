package org.activityinfo.ui.client.component.form.field;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.map.ChooseReferenceMapDialog;
import org.activityinfo.ui.client.component.form.field.map.MapItem;
import org.activityinfo.ui.client.widget.Button;
import org.activityinfo.ui.client.widget.TextBox;

import java.util.Set;

/**
 * Created by yuriyz on 9/13/2016.
 */
public class ReferenceMapWidget implements FormFieldWidget<ReferenceValue> {

    interface GeographicPointMapWidgetUiBinder extends UiBinder<HTMLPanel, ReferenceMapWidget> {
    }

    private static GeographicPointMapWidgetUiBinder ourUiBinder = GWT.create(GeographicPointMapWidgetUiBinder.class);

    private final HTMLPanel panel;
    private final ValueUpdater<ReferenceValue> valueUpdater;
    private final Set<MapItem> items;

    private ReferenceValue value;

    @UiField
    Button changeButton;
    @UiField
    TextBox labelBox;

    public ReferenceMapWidget(Set<MapItem> items, final ValueUpdater<ReferenceValue> valueUpdater) {
        this.items = items;
        this.valueUpdater = valueUpdater;

        this.panel = ourUiBinder.createAndBindUi(this);

        this.changeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showMap();
            }
        });
    }

    private void showMap() {
        ChooseReferenceMapDialog dialog = new ChooseReferenceMapDialog(items);
        dialog.show(new SelectionHandler<ReferenceValue>() {
            @Override
            public void onSelection(SelectionEvent<ReferenceValue> event) {
                setValue(event.getSelectedItem());
                fireValueChanged();
            }
        });
    }

    @Override
    public void fireValueChanged() {
        valueUpdater.update(value);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        changeButton.setEnabled(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return !changeButton.isEnabled();
    }

    @Override
    public Promise<Void> setValue(ReferenceValue value) {
        this.value = value;
        labelBox.setText(value == null ? "" : getLabel(value));
        return Promise.done();
    }

    private String getLabel(ReferenceValue value) {        ;
        return MapItem.byId(items, value.getOnlyReference().getRecordId().asString()).get().getLabel();
    }

    @Override
    public void clearValue() {
        setValue(null);
    }

    @Override
    public void setType(FieldType type) {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
