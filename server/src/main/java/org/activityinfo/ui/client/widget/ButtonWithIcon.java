package org.activityinfo.ui.client.widget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;
import org.activityinfo.ui.client.style.ElementStyle;

/**
 * Button with an icon
 */
public class ButtonWithIcon extends Button {


    public interface Templates extends SafeHtmlTemplates {
        @Template("<span class=\"{0}\"></span> {1}")
        SafeHtml withIcon(String styleNames, String text);

        @Template("<span class=\"{0}\"></span>")
        SafeHtml withIcon(String styleNames);
    }

    public static Templates TEMPLATES = GWT.create(Templates.class);

    @UiConstructor
    public ButtonWithIcon(ElementStyle style, String iconStyle, String text) {
        super(style);
        setHTML(TEMPLATES.withIcon(iconStyle, text));
    }
}
