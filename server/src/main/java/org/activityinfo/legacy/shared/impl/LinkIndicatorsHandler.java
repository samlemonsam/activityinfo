package org.activityinfo.legacy.shared.impl;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.bedatadriven.rebar.sql.client.query.SqlInsert;
import com.bedatadriven.rebar.sql.client.query.SqlUpdate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.LinkIndicators;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;

public class LinkIndicatorsHandler implements CommandHandlerAsync<LinkIndicators, VoidResult> {

    @Override
    public void execute(LinkIndicators command, ExecutionContext context, AsyncCallback<VoidResult> callback) {

        if(command.getDestIndicatorId() == command.getSourceIndicatorId()) {
            throw new CommandException("An indicator cannot be linked to itself.");
        }

        SqlUpdate.delete(Tables.INDICATOR_LINK)
                 .where("sourceIndicatorId", command.getSourceIndicatorId())
                 .where("destinationIndicatorId", command.getDestIndicatorId())
                 .execute(context.getTransaction());

        if (command.isLink()) {
            SqlInsert.insertInto(Tables.INDICATOR_LINK)
                     .value("sourceIndicatorId", command.getSourceIndicatorId())
                     .value("destinationIndicatorId", command.getDestIndicatorId())
                     .execute(context.getTransaction());
        }

        callback.onSuccess(null);

    }

}
