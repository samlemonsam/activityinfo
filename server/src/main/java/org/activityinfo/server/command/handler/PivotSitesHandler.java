/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.command.handler;

import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.UnexpectedCommandException;
import org.activityinfo.server.command.handler.pivot.PivotAdapter;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.spi.UserDatabaseProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.SQLException;
import java.util.logging.Logger;


public class PivotSitesHandler implements CommandHandler<PivotSites> {
    
    private static final Logger LOGGER = Logger.getLogger(PivotSitesHandler.class.getName());

    @Inject
    private Provider<MySqlStorageProvider> catalog;

    @Inject
    private UserDatabaseProvider userDatabaseProvider;

    @Override
    public CommandResult execute(final PivotSites cmd, final User user) throws CommandException {

        PivotAdapter adapter;
        try {
            adapter = new PivotAdapter(catalog.get(), userDatabaseProvider, user.getId(), cmd);
            return adapter.execute();

        } catch (InterruptedException e) {
            throw new CommandException("Interrupted");
        } catch (SQLException e) {
            throw new UnexpectedCommandException(e);
        }
    }
}
