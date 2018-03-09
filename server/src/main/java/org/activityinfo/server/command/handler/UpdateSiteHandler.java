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

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.UpdateSite;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.handler.crud.PropertyMap;
import org.activityinfo.server.database.hibernate.entity.User;


@SuppressWarnings("unused")
public class UpdateSiteHandler implements CommandHandler<UpdateSite> {
    
    private final SiteUpdate siteUpdate;

    @Inject
    public UpdateSiteHandler(SiteUpdate createSiteHandler) {
        this.siteUpdate = createSiteHandler;
    }

    @Override
    public CommandResult execute(UpdateSite cmd, User user) throws CommandException {
        
        siteUpdate.createOrUpdateSite(user, cmd.getSiteId(), new PropertyMap(cmd.getProperties()));
        
        return VoidResult.EMPTY;
    }
}
