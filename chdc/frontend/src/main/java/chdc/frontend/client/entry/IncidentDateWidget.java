package chdc.frontend.client.entry;

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.view.field.FieldUpdater;
import org.activityinfo.ui.client.input.view.field.FieldWidget;

/**
 * A simplified date widget that does not use a date popup.
 */
public class IncidentDateWidget implements FieldWidget {

    private final TextField field;

    public IncidentDateWidget(FormField formField, FieldUpdater updater) {
        field = new TextField(new TextInputCell(new TextAppearance(formField.getLabel())));
        field.addKeyUpHandler(event -> updater.update(input()));
        field.setWidth(-1);
    }

    private FieldInput input() {
        String value = field.getText();
        if(Strings.isNullOrEmpty(value)) {
            return FieldInput.EMPTY;
        } else {
            try {
                return new FieldInput(LocalDate.parse(value));
            } catch (NumberFormatException e) {
                return FieldInput.INVALID_INPUT;
            }
        }
    }

    @Override
    public void init(FieldValue value) {
        if(value instanceof LocalDate) {
            field.setText(value.toString());
        }
    }

    @Override
    public void clear() {
        field.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        field.setReadOnly(!relevant);
    }

    @Override
    public Widget asWidget() {
        return field;
    }
}
