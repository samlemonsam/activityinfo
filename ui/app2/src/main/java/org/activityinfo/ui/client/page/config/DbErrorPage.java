package org.activityinfo.ui.client.page.config;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.Page;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.entry.SuspendAccountPanel;

public class DbErrorPage implements Page {

    private static final PageId SUSPENDED_PAGEID = new PageId("SUSPENDED");
    private final ContentPanel panel;

    public DbErrorPage(UserDatabaseDTO database) {
        panel = SuspendAccountPanel.createPanel(database.getAmOwner());
    }

    @Override
    public PageId getPageId() {
        return SUSPENDED_PAGEID;
    }

    @Override
    public Object getWidget() {
        return panel;
    }

    @Override
    public void requestToNavigateAway(PageState place, NavigationCallback callback) {
        callback.onDecided(true);
    }

    @Override
    public String beforeWindowCloses() {
        return null;
    }

    @Override
    public boolean navigate(PageState place) {
        return false;
    }

    @Override
    public void shutdown() {
    }
}
