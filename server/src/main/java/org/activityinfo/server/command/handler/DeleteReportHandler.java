package org.activityinfo.server.command.handler;

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

import com.bedatadriven.rebar.sql.client.query.SqlUpdate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.DeleteReport;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.impl.CommandHandlerAsync;
import org.activityinfo.legacy.shared.impl.ExecutionContext;
import org.activityinfo.legacy.shared.impl.Tables;

public class DeleteReportHandler implements CommandHandlerAsync<DeleteReport, VoidResult> {

    @Override
    public void execute(DeleteReport command, ExecutionContext context, AsyncCallback<VoidResult> callback) {
        SqlUpdate.delete(Tables.REPORT_TEMPLATE)
                 .where("reporttemplateid", command.getReportId())
                 .execute(context.getTransaction());
        SqlUpdate.delete(Tables.REPORT_SUBSCRIPTION)
                 .where("reportId", command.getReportId())
                 .execute(context.getTransaction());
        SqlUpdate.delete(Tables.REPORT_VISIBILITY)
                 .where("reportId", command.getReportId())
                 .execute(context.getTransaction());

        UpdateReportModelHandler.invalidateMemcache(command.getReportId());

        callback.onSuccess(null);
    }

}
