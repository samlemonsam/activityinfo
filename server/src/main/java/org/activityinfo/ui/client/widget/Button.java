package org.activityinfo.ui.client.widget;

import com.google.gwt.uibinder.client.UiConstructor;
import org.activityinfo.ui.client.style.ElementStyle;

/**
 * Subclass of {@link org.activityinfo.ui.client.widget.ButtonWithSize} that allows to create button without size
 * definition (default).
 */
public class Button extends ButtonWithSize {

    @UiConstructor
    public Button(ElementStyle style) {
        super(style, null);
    }

    public void setLabel(String label) {
        super.setHTML(label);
    }
}
