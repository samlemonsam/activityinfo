package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.form.TextArea;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.ui.client.input.model.FieldInput;

/**
 * FieldWidget for {@link org.activityinfo.model.type.NarrativeType} fields.
 */
public class NarrativeWidget implements FieldWidget {

    private TextArea textArea;

    public NarrativeWidget(FieldUpdater updater) {
        textArea = new TextArea();
        textArea.addKeyUpHandler(event -> updater.update(input()));
    }

    private FieldInput input() {
        return new FieldInput(NarrativeValue.valueOf(textArea.getText()));
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
