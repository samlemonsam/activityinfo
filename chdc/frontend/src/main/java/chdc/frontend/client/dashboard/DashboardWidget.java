package chdc.frontend.client.dashboard;

import chdc.frontend.client.theme.ActionBar;
import chdc.frontend.client.theme.MainContainer;
import chdc.frontend.client.theme.QuickSearchForm;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Contains the data entry dashboard
 */
public class DashboardWidget implements IsWidget {

    private MainContainer container;

    public DashboardWidget() {

        ActionBar actionBar = new ActionBar();
        actionBar.addShortcut(new QuickSearchForm());

        this.container = new MainContainer();
        this.container.add(new DashboardSidebar());
        this.container.add(actionBar);
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
