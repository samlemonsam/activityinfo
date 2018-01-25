package chdc.frontend.client.dashboard;

import chdc.frontend.client.Forms;
import chdc.frontend.client.entry.DataEntryPlace;
import chdc.frontend.client.i18n.ChdcLabels;
import chdc.frontend.client.table.TablePlace;
import chdc.frontend.client.theme.Icon;
import chdc.frontend.client.theme.NavContainer;
import chdc.frontend.client.theme.NavFooter;
import chdc.frontend.client.theme.NavList;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;

public class DashboardSidebar implements IsWidget {

    private final FlowLayoutContainer sidebar;

    public DashboardSidebar() {

        NavList navList = new NavList.Builder()
            .addIconLink(Icon.PLUS, new DataEntryPlace(Forms.INCIDENT), ChdcLabels.LABELS.addSingleIncident())
            .addIconLink(Icon.TABLE, new TablePlace(Forms.INCIDENT), ChdcLabels.LABELS.bulkEdit())
            .addIconLink(Icon.LIST, new DashboardPlace(), ChdcLabels.LABELS.showAllIncidents())
            .addIconLink(Icon.SEARCH, new DashboardPlace(), ChdcLabels.LABELS.searchInIncidents())
            .build();

        NavContainer nav = new NavContainer();
        nav.setStyleName("navigation");
        nav.add(new NavHeader());
        nav.add(navList);
        nav.add(new NavFooter());

        this.sidebar = new FlowLayoutContainer();
        this.sidebar.setStyleName("sidebar");
        this.sidebar.add(nav);
    }

    @Override
    public Widget asWidget() {
        return sidebar;
    }
}
