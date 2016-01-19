package org.activityinfo.server.command.handler;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.OldPivotSites;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.command.handler.pivot.IndicatorOracle;
import org.activityinfo.server.command.handler.pivot.PivotAdapter;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.service.store.CollectionCatalog;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.logging.Logger;


public class PivotSitesHandler implements CommandHandler<PivotSites> {
    
    private static final Logger LOGGER = Logger.getLogger(PivotSitesHandler.class.getName());

    @Inject
    private IndicatorOracle indicatorOracle;

    @Inject
    private Provider<CollectionCatalog> catalog;

    @Inject
    private DispatcherSync dispatcher;
    
    @Override
    public CommandResult execute(final PivotSites cmd, final User user) throws CommandException {
        
        if(cmd.isPointRequested()) {

            LOGGER.info("Falling back to old query engine for geographic query.");

            // this is the only thing that is not working, so fall back to the old pivot sites handler
            return dispatcher.execute(new OldPivotSites(cmd));

        } else if(cmd.isPivotedBy(DimensionType.Target)) {
            LOGGER.info("Falling back to old query engine for query that includes targets.");
            return dispatcher.execute(new OldPivotSites(cmd));

        }
        
        PivotAdapter adapter = null;
        try {
            adapter = new PivotAdapter(indicatorOracle, catalog.get(), cmd);
        } catch (InterruptedException e) {
            throw new CommandException("Interrupted");
        }
        return adapter.execute();
    }
}
