package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.Event;
import com.sencha.gxt.widget.core.client.form.TextField;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.input.model.TextInput;

public class TextWidget extends TextField {

    private TextInput input;

    private String currentText;
    private Subscription subscription;

    public TextWidget(TextInput input) {
        this.input = input;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        subscription = input.getInput().subscribe(observable -> {
            if (observable.isLoaded() && !observable.get().equals(currentText)) {
                setText(observable.get());
            }
        });
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        subscription.unsubscribe();
    }

    @Override
    protected void onKeyPress(Event event) {
        super.onKeyPress(event);
        currentText = getText();
        input.update(currentText);
    }
}
