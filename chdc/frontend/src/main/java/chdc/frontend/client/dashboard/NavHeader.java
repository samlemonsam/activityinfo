package chdc.frontend.client.dashboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Header widget for the dashboard sidebar
 */
public class NavHeader implements IsWidget {

    interface Templates extends SafeHtmlTemplates {
        @Template("<h2 class=\"twoliner\">Welcome back<strong>{0}</strong></h2>")
        SafeHtml header(String userName);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private final HTML html;

    public NavHeader() {
        this.html = new HTML();
        this.html.setStyleName("navigation__header");
        this.html.setHTML(TEMPLATES.header("Jan Rapp"));
    }

    @Override
    public Widget asWidget() {
        return html;
    }
}
