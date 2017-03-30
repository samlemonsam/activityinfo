package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import org.activityinfo.ui.client.table.viewModel.TableViewModel;

/**
 * Sidebar panel containing details, history, etc.
 */
public class SidePanel extends ContentPanel {

    public SidePanel(TableViewModel viewModel) {
        setHeading("Side Panel");
        setHeaderVisible(false);
        TabPanel tabPanel = new TabPanel();
        tabPanel.add(new DetailsPane(viewModel), new TabItemConfig("Details"));

        add(tabPanel);
    }

}
