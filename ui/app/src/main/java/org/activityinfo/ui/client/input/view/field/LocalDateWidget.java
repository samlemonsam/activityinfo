package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.form.DateField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.input.model.FieldInput;

import java.util.Collections;
import java.util.List;

/**
 * FieldWidget for {@link org.activityinfo.model.type.time.LocalDateType} fields.
 */
public class LocalDateWidget implements PeriodFieldWidget {

    private DateField field;

    public LocalDateWidget(FieldUpdater fieldUpdater) {
        this.field = new DateField();
        this.field.addValueChangeHandler(event -> fieldUpdater.update(input()));
    }

    private FieldInput input() {
        if(field.isValid()) {
            if(field.getValue() == null) {
                return FieldInput.EMPTY;
            } else {
                return new FieldInput(new LocalDate(field.getValue()));
            }
        } else {
            return FieldInput.INVALID_INPUT;
        }
    }

    @Override
    public void init(FieldValue value) {
        field.setValue(((LocalDate) value).atMidnightInMyTimezone());
    }

    @Override
    public void clear() {
        field.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        field.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return field;
    }

    @Override
    public List<Component> asToolBarItems() {
        return Collections.singletonList(field);
    }
}
