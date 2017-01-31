package org.activityinfo.ui.client.widget;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.HeaderPanel;

/**
 * Subclasses {@link com.google.gwt.user.client.ui.HeaderPanel} to add
 * our application styles
 *
 * @see <a href="http://getbootstrap.com/components/#panels">Bootstrap Panels</a>
 */
public class Panel extends HeaderPanel {

    public enum Style {
        PRIMARY,
        SUCCESS,
        INFO,
        WARNING,
        DANGER
    }

    @UiConstructor
    public Panel(Style style) {

        addStyleName("panel panel-" + style.name().toLowerCase());

        DivElement header = getElement().getChild(0).cast();
        header.setClassName("panel-heading");

        DivElement footer = getElement().getChild(1).cast();
        footer.setClassName("panel-footer");

        DivElement body = getElement().getChild(2).cast();
        body.setClassName("panel-body");
    }
}
