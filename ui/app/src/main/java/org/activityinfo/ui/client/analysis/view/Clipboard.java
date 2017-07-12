package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;

/**
 * Supports copying to the system clipboard if supported.
 *
 * <p>Based on https://github.com/zenorocha/clipboard.js</p>
 */
public class Clipboard {


    private Clipboard() {
    }

    /**
     * Copies the provided {@code text} to the system clipboard.
     * @param text the text to copy
     * @return the
     */
    public static boolean copy(String text) {

        Document document = Document.get();

        TextAreaElement element = document.createTextAreaElement();

        // Prevent zooming on iOS
        Style style = element.getStyle();
        style.setFontSize(12, Style.Unit.PT);

        // Reset box model
        style.setBorderWidth(0, Style.Unit.PX);
        style.setMargin(0, Style.Unit.PX);
        style.setPadding(0, Style.Unit.PX);

        // Move element out of screen horizontally
        style.setPosition(Style.Position.ABSOLUTE);
        if(LocaleInfo.hasAnyRTL()) {
            style.setRight(-9999, Style.Unit.PX);
        } else {
            style.setLeft(-9999, Style.Unit.PX);
        }

        // Move element to the same position vertically
        int yPosition = Window.getScrollTop();
        style.setTop(yPosition, Style.Unit.PX);

        element.setReadOnly(false);
        element.setValue(text);

        Document.get().getBody().appendChild(element);

        element.select();

        boolean succeeded;
        try {
            executeCopy();
            succeeded = true;
        } catch (Exception e) {
            succeeded = false;
        }

        element.removeFromParent();

        return succeeded;
    }

    private static final native void executeCopy() /*-{
        $wnd.document.execCommand("copy");
    }-*/;

}
