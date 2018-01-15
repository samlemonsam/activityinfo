package chdc.frontend.client.theme;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.Widget;

/**
 * A hyperlink styled as a button
 */
public class LinkButton extends Widget {

    public LinkButton(String label, SafeUri href) {
        AnchorElement link = Document.get().createAnchorElement();
        link.addClassName("button");
        link.setHref(href);
        link.setInnerText(label);
        setElement(link);
    }

}
