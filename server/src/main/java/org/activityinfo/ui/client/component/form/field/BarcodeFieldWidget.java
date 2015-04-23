package org.activityinfo.ui.client.component.form.field;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.barcode.BarcodeValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.widget.TextBox;

public class BarcodeFieldWidget implements FormFieldWidget<BarcodeValue> {

    private final TextBox box;

    private static final String PLACEHOLDER_TEXT = "ABCDEF0123456";

    public BarcodeFieldWidget(final ValueUpdater<BarcodeValue> valueUpdater) {
        this.box = new TextBox();
        this.box.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                valueUpdater.update(BarcodeValue.valueOf(event.getValue()));
            }

        });
        this.box.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                valueUpdater.update(getValue());
            }
        });
    }

    private BarcodeValue getValue() {
        return BarcodeValue.valueOf(BarcodeFieldWidget.this.box.getValue());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        box.setReadOnly(readOnly);
    }

    @Override
    public Promise<Void> setValue(BarcodeValue value) {
        box.setValue(value.getCode());
        return Promise.done();
    }

    @Override
    public void clearValue() {
        box.setValue(null);
    }

    @Override
    public void setType(FieldType type) {

    }

    @Override
    public Widget asWidget() {
        return box;
    }
}
