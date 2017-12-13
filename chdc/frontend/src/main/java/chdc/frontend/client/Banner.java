package chdc.frontend.client;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class Banner implements IsWidget {

    private HTML html;

    public Banner() {
        this.html = new HTML(SafeHtmlUtils.fromTrustedString("<h1><a href=\"/\"><span>INSO</span> CHDC</a></h1>"));
        this.html.addStyleName(ChdcResources.RESOURCES.getStyle().banner());
    }

    @Override
    public Widget asWidget() {
        return html;
    }
}
