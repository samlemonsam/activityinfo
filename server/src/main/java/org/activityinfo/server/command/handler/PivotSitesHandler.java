package org.activityinfo.server.command.handler;

import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.handler.pivot.PivotAdapter;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.service.store.CollectionCatalog;

import javax.inject.Inject;
import javax.inject.Provider;


public class PivotSitesHandler implements CommandHandler<PivotSites> {

    @Inject
    private IndicatorOracle indicatorOracle;

    @Inject
    private Provider<CollectionCatalog> catalog;

    @Override
    public CommandResult execute(final PivotSites cmd, final User user) throws CommandException {
        PivotAdapter adapter = null;
        try {
            adapter = new PivotAdapter(indicatorOracle, catalog.get(), cmd);
        } catch (InterruptedException e) {
            throw new CommandException("Interrupted");
        }
        return adapter.execute();
    }
}
