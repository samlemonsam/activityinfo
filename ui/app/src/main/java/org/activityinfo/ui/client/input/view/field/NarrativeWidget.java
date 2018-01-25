package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.TextAreaInputCell;
import com.sencha.gxt.widget.core.client.form.TextArea;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.ui.client.input.model.FieldInput;

/**
 * FieldWidget for {@link org.activityinfo.model.type.NarrativeType} fields.
 */
public class NarrativeWidget implements FieldWidget {

    private TextArea textArea;

    public NarrativeWidget(FieldUpdater updater) {
        this(GWT.create(TextAreaInputCell.TextAreaAppearance.class), updater);
    }

    public NarrativeWidget(TextAreaInputCell.TextAreaAppearance appearance, FieldUpdater updater) {
        textArea = new TextArea(new TextAreaInputCell(appearance));
        textArea.addKeyUpHandler(event -> updater.update(input()));
        textArea.setWidth(-1);
    }

    private FieldInput input() {
        return new FieldInput(NarrativeValue.valueOf(textArea.getText()));
    }

    @Override
    public void init(FieldValue value) {
        textArea.setText(((NarrativeValue) value).asString());
    }

    @Override
    public void clear() {
        textArea.clear();
    }

    @Override
    public void setRelevant(boolean relevant) {
        textArea.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return textArea;
    }
}
