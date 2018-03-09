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
import org.activityinfo.legacy.shared.command.RequestChange;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.entity.change.ChangeHandler;
import org.activityinfo.server.entity.change.ChangeRequestBuilder;
import org.activityinfo.server.entity.change.ChangeType;

public class RequestChangeHandler implements CommandHandler<RequestChange> {

    private final ChangeHandler changeHandler;

    @Inject
    public RequestChangeHandler(ChangeHandler changeHandler) {
        super();
        this.changeHandler = changeHandler;
    }

    @Override
    public CommandResult execute(RequestChange cmd, User user) throws CommandException {

        ChangeRequestBuilder request = new ChangeRequestBuilder().setChangeType(ChangeType.valueOf(cmd.getChangeType()))
                                                                 .setEntityId(cmd.getEntityId())
                                                                 .setEntityType(cmd.getEntityType())
                                                                 .setUser(user);

        if (cmd.getPropertyMap() != null) {
            request.setProperties(cmd.getPropertyMap().getTransientMap());
        }

        changeHandler.execute(request);

        return new VoidResult();
    }
}
