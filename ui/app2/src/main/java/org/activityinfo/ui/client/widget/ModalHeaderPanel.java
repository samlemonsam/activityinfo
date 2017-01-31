package org.activityinfo.ui.client.widget;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.ui.HeaderPanel;

/**
 * Subclasses {@link com.google.gwt.user.client.ui.HeaderPanel} to apply
 * the Bootstrap Modal styles. This class only styles and sizes the panel,
 * it doesn't handle creating the popup.
 *
 * @see <a href="http://getbootstrap.com/javascript/#modals">Bootstrap Modals</a>
 */
public class ModalHeaderPanel extends HeaderPanel {


    public ModalHeaderPanel() {
        DivElement header = getElement().getChild(0).cast();
        header.setClassName("modal-header");

        DivElement footer = getElement().getChild(1).cast();
        footer.setClassName("modal-footer");

        DivElement body = getElement().getChild(2).cast();
        body.setClassName("modal-body");
    }
}
