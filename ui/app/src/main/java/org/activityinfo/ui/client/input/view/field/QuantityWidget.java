package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.DoubleField;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.ui.client.input.model.FieldInput;

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
        this.field.addKeyUpHandler(event -> updater.update(input()));

        InlineHTML units = new InlineHTML(SafeHtmlUtils.fromString(quantityType.getUnits()));

        container = new FlowPanel("span");
        container.add(field);
        container.add(units);
    }

    private FieldInput input() {
        if(field.isValid()) {
            if(field.getValue() == null) {
                return FieldInput.EMPTY;
            } else {
                return new FieldInput(new Quantity(field.getValue()));
            }
        } else {
            return FieldInput.INVALID_INPUT;
        }
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
