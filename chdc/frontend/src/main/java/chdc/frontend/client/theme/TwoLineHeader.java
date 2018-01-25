package chdc.frontend.client.theme;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;

public class TwoLineHeader extends Widget {

    public TwoLineHeader(SafeHtml html) {
        Element header = Document.get().createElement("h2");
        header.addClassName("twoliner");
        header.setInnerSafeHtml(html);
        setElement(header);
    }
}
