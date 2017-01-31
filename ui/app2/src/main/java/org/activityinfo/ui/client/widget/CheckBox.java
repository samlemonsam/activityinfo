package org.activityinfo.ui.client.widget;

import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Subclass of {@code CheckBox} that applies our application styles
 */
public class CheckBox extends com.google.gwt.user.client.ui.CheckBox {

    public CheckBox() {
        setStyleName("checkbox");
    }

    public CheckBox(String label) {
        this();
        setText(label);
    }

    public CheckBox(SafeHtml label) {
        this();
        setHTML(label);
    }
}
