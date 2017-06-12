package org.activityinfo.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Link to the ActivityInfo user manual
 */
public class HelpLink implements IsWidget {

    interface Templates extends SafeHtmlTemplates {
        @Template("<a href=\"http://help.activityinfo.org/m/28175/l/{0}\" target=\"_blank\">{1}</a>")
        SafeHtml link(int helpId, String label);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private HTML html;

    /**
     * @param label the label of thel ink
     * @param helpId the ScreenSteps id
     */
    @UiConstructor
    public HelpLink(String label, int helpId) {
        this.html = new HTML(TEMPLATES.link(helpId, label));
    }

    @Override
    public Widget asWidget() {
        return html;
    }
}
