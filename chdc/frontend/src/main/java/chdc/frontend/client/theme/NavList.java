package chdc.frontend.client.theme;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;

/**
 * Unordered list of navigation links.
 */
public class NavList extends Widget {

    private NavList(SafeHtml html) {
        UListElement element = Document.get().createULElement();
        element.setClassName("navigation");
        element.setInnerSafeHtml(html);
        setElement(element);
    }

    public static class Builder extends FlowLayoutContainer {

        private SafeHtmlBuilder html = new SafeHtmlBuilder();

        public Builder addIconLink(Icon icon, Place place, String label) {
            html.appendHtmlConstant("<li>");
            html.appendHtmlConstant("<a href=\"#" + place.toString() + "\">");
            html.append(icon.svg());
            html.appendHtmlConstant("<span>");
            html.appendEscaped(label);
            html.appendHtmlConstant("</span>");
            html.appendHtmlConstant("</li>");
            return this;
        }

        public NavList build() {
            return new NavList(html.toSafeHtml());
        }
    }
}
