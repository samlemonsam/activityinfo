package org.activityinfo.ui.client.input.view.field;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.NumberInputCell;
import com.sencha.gxt.widget.core.client.form.DoubleField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.text.ParseException;

/**
 * FieldWidget for {@link QuantityType} fields.
 */
public class QuantityWidget implements FieldWidget {

    private DoubleField field;

    public QuantityWidget(QuantityType quantityType, FieldUpdater updater) {
        this.field = new DoubleField(new NumberInputCell<>(
                new NumberPropertyEditor.DoublePropertyEditor(),
                new QuantityFieldAppearance(quantityType.getUnits())));
        this.field.addKeyUpHandler(event -> Scheduler.get().scheduleDeferred(() -> {
            updater.update(input());
        }));

    }

    private FieldInput input() {
        String text = field.getText();
        if(Strings.isNullOrEmpty(text)) {
            return FieldInput.EMPTY;
        } else {
            double doubleValue;
            try {
                doubleValue = field.getPropertyEditor().parse(text);
                return new FieldInput(new Quantity(doubleValue));
            } catch (ParseException e) {
                return FieldInput.INVALID_INPUT;
            }
        }
    }

    @Override
    public void init(FieldValue value) {
        this.field.setValue(((Quantity) value).getValue());
    }

    @Override
    public void clear() {
        field.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        this.field.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return field;
    }
}
