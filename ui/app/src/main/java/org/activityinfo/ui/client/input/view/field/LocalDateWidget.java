package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.DateField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.input.model.FieldInput;

/**
 * FieldWidget for {@link org.activityinfo.model.type.time.LocalDateType} fields.
 */
public class LocalDateWidget implements FieldWidget {

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
    public void setRelevant(boolean relevant) {
        field.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return field;
    }
}
