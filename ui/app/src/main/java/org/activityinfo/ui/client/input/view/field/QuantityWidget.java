package org.activityinfo.ui.client.input.view.field;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.DoubleField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.text.ParseException;

/**
 * FieldWidget for {@link QuantityType} fields.
 */
public class QuantityWidget implements FieldWidget {

    private FlowPanel container;
    private DoubleField field;
    private QuantityType quantityType;

    public QuantityWidget(QuantityType quantityType, FieldUpdater updater) {
        this.quantityType = quantityType;

        this.field = new DoubleField();
        this.field.addKeyUpHandler(event -> Scheduler.get().scheduleDeferred(() -> {
            updater.update(input());
        }));
        InlineHTML units = new InlineHTML(SafeHtmlUtils.fromString(quantityType.getUnits()));

        container = new FlowPanel("span");
        container.add(field);
        container.add(units);
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
        return container;
    }
}
