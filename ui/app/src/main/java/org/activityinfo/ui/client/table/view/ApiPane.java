package org.activityinfo.ui.client.table.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.XTemplates;
import org.activityinfo.analysis.table.ApiViewModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;

public class ApiPane implements IsWidget {

    private HTML panel;

    interface Templates extends XTemplates {

        @XTemplate(source = "ApiPane.html")
        SafeHtml panel(TableBundle.Style style, ApiViewModel model);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    public ApiPane(TableViewModel viewModel) {

        this.panel = new HTML("");
        this.panel.addStyleName(TableBundle.INSTANCE.style().detailPane());

        viewModel.getApiViewModel().subscribe(this::render);
    }

    private void renderLoading() {
        panel.setHTML(SafeHtmlUtils.htmlEscape(I18N.CONSTANTS.loading()));
    }

    private void render(Observable<ApiViewModel> observable) {
        if(observable.isLoaded()) {
            render(observable.get());
        } else {
            panel.setHTML(SafeHtmlUtils.htmlEscape(I18N.CONSTANTS.loading()));
        }
    }

    private void render(ApiViewModel apiViewModel) {
        panel.setHTML(TEMPLATES.panel(TableBundle.INSTANCE.style(), apiViewModel));
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
