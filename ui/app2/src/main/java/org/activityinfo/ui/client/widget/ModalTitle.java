package org.activityinfo.ui.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * Modal Title widget intended for use with {@code ModalHeaderPanel}
 */
public class ModalTitle extends Widget implements HasText {

    public ModalTitle() {
        setElement(Document.get().createHElement(4));
        setStyleName("modal-title");
    }

    @Override
    public String getText() {
        return getElement().getInnerText();
    }

    @Override
    public void setText(String text) {
        getElement().setInnerText(text);
    }
}
