package org.activityinfo.ui.client.page.entry;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.ui.client.page.entry.column.GridLayout;

public class SuspendAccountPanel implements SiteGridPanelView {

    private ContentPanel panel;


    interface Templates extends SafeHtmlTemplates {

        @Template("<h1>{0}</h1><br>{1}")
        SafeHtml panel(String heading, String message);
    }

    SuspendAccountPanel(final GridLayout layout) {
        this.panel = createPanel(layout.isOwner());
    }

    public static ContentPanel createPanel(boolean owner) {
        ContentPanel panel = new ContentPanel();
        panel.setHeaderVisible(false);
        panel.setLayout(new CenterLayout());
        panel.setBodyStyle("font-family: sans-serif;");

        Templates templates = GWT.create(Templates.class);

        if(owner) {
            panel.add(new HTML(templates.panel(I18N.CONSTANTS.trialAccountExpired(), I18N.CONSTANTS.trialAccountExpiredMessage())));
        } else {
            panel.add(new HTML(templates.panel(I18N.CONSTANTS.accessExpired(), I18N.CONSTANTS.accessExpiredMessage())));
        }
        return panel;
    }

    @Override
    public void addSelectionChangeListener(SelectionChangedListener<SiteDTO> listener) {
        return;
    }

    @Override
    public void addDoubleClickListener(SelectionChangedListener<SiteDTO> listener) {
        return;
    }

    @Override
    public void refresh() {
        return;
    }

    @Override
    public Component asComponent() {
        return panel;
    }

    @Override
    public SiteDTO getSelection() {
        return null;
    }
}
