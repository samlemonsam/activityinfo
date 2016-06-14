package org.activityinfo.server.command.handler;

import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.UnexpectedCommandException;
import org.activityinfo.server.command.handler.pivot.PivotAdapter;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.service.store.CollectionCatalog;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.SQLException;
import java.util.logging.Logger;


public class PivotSitesHandler implements CommandHandler<PivotSites> {
    
    private static final Logger LOGGER = Logger.getLogger(PivotSitesHandler.class.getName());

    @Inject
    private Provider<CollectionCatalog> catalog;

    @Override
    public CommandResult execute(final PivotSites cmd, final User user) throws CommandException {

        PivotAdapter adapter;
        try {
            adapter = new PivotAdapter(catalog.get(), cmd, user.getId());
            return adapter.execute();

        } catch (InterruptedException e) {
            throw new CommandException("Interrupted");
        } catch (SQLException e) {
            throw new UnexpectedCommandException(e);
        }
    }
}
