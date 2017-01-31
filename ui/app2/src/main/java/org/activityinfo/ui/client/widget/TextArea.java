package org.activityinfo.ui.client.widget;

/**
 * Subclass of {@link com.google.gwt.user.client.ui.TextArea} that applies our application styles
 *
 */
public class TextArea extends com.google.gwt.user.client.ui.TextArea {

    public TextArea() {
        setStyleName("form-control");
    }

    public void setPlaceholder(String text) {
        getElement().setAttribute("placeholder", text);
    }
}
