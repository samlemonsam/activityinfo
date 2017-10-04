package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.OldGetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.mysql.MySqlCatalog;

import javax.inject.Provider;

public class GetSitesHandler implements CommandHandler<GetSites> {

    @Inject
    private Provider<MySqlCatalog> catalog;

    @Inject
    private DispatcherSync dispatcher;

    @Override
    public SiteResult execute(GetSites command, User user) {

        if(command.isLegacyFetch()) {
            return dispatcher.execute(new OldGetSites(command));
        }

        return new SiteResult();
    }

}
