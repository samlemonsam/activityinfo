package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Sidebar panel containing details, history, etc.
 */
public class SidePanel extends ContentPanel {

    public SidePanel(FormStore formStore, TableViewModel viewModel) {
        setHeading("Side Panel");
        setHeaderVisible(false);
        TabPanel tabPanel = new TabPanel();
        tabPanel.add(new DetailsPane(viewModel), new TabItemConfig("Details"));
        tabPanel.add(new HistoryPane(formStore, viewModel), new TabItemConfig(I18N.CONSTANTS.history()));
        add(tabPanel);
    }

}
