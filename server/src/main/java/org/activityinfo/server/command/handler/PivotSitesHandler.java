package org.activityinfo.server.command.handler;

import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.handler.pivot.PivotAdapter;
import org.activityinfo.server.database.hibernate.HibernateQueryExecutor;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.service.store.CollectionCatalog;

import javax.inject.Inject;


public class PivotSitesHandler implements CommandHandler<PivotSites> {
    
    @Inject
    private HibernateQueryExecutor queryExecutor;
    
    @Inject
    private IndicatorOracle indicatorOracle;
    
    @Override
    public CommandResult execute(final PivotSites cmd, final User user) throws CommandException {
        return queryExecutor.joinTxAndDoWork(new HibernateQueryExecutor.StoreSession<PivotSites.PivotResult>() {
            @Override
            public PivotSites.PivotResult execute(CollectionCatalog catalog) {
                PivotAdapter adapter = new PivotAdapter(indicatorOracle, catalog, cmd);
                return adapter.execute();
            }
        });
    }
}
