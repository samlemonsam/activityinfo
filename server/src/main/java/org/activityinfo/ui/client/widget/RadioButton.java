package org.activityinfo.ui.client.widget;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Event;

/**
 * Subclass of {@link RadioButton} that applies our application styles
 */
public class RadioButton extends com.google.gwt.user.client.ui.RadioButton {

    @UiConstructor
    public RadioButton(String name) {
        super(name);
        setStyleName("radio");
    }

    public RadioButton(String name, String label) {
        super(name, label);
        setStyleName("radio");
    }

    public RadioButton(String name, SafeHtml label) {
        super(name, label);
        setStyleName("radio");
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        if (event.getCtrlKey() && event.getTypeInt() == Event.ONCLICK && getValue()) {
            setValue(false, true);
        }
    }
}
