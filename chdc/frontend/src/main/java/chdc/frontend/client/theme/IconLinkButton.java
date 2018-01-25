package chdc.frontend.client.theme;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.Widget;

public class IconLinkButton extends Widget {


    interface Templates extends SafeHtmlTemplates {
        @Template("{0} <span>{1}</span>")
        SafeHtml label(SafeHtml icon, String label);
    }

    public static final Templates TEMPLATES = GWT.create(Templates.class);

    public IconLinkButton(Icon icon, String label, SafeUri href) {
        AnchorElement link = Document.get().createAnchorElement();
        link.addClassName("button");
        link.addClassName("button--icon");
        link.setHref(href);
        link.setInnerSafeHtml(TEMPLATES.label(icon.svg(), label));
        setElement(link);
    }
}
