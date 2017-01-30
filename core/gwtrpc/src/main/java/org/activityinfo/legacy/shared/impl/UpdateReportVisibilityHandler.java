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
import org.activityinfo.legacy.shared.command.UpdateReportVisibility;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.ReportVisibilityDTO;

public class UpdateReportVisibilityHandler implements CommandHandlerAsync<UpdateReportVisibility, VoidResult> {

    @Override
    public void execute(UpdateReportVisibility command, ExecutionContext context, AsyncCallback<VoidResult> callback) {

        SqlUpdate.delete(Tables.REPORT_VISIBILITY)
                 .where("reportid", command.getReportId())
                 .execute(context.getTransaction());
        for (ReportVisibilityDTO dto : command.getList()) {
            SqlUpdate.delete(Tables.REPORT_VISIBILITY)
                     .where("reportid", command.getReportId())
                     .where("databaseid", dto.getDatabaseId())
                     .execute(context.getTransaction());

            if (dto.isVisible()) {
                SqlInsert.insertInto(Tables.REPORT_VISIBILITY)
                         .value("reportid", command.getReportId())
                         .value("databaseid", dto.getDatabaseId())
                         .value("defaultDashboard", dto.isDefaultDashboard())
                         .execute(context.getTransaction());
            }
        }
        callback.onSuccess(null);

    }
}
