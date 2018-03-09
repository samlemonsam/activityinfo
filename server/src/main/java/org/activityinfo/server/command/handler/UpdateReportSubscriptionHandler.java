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

import com.bedatadriven.rebar.sql.client.SqlResultCallback;
import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.query.SqlInsert;
import com.bedatadriven.rebar.sql.client.query.SqlUpdate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.UpdateReportSubscription;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.impl.CommandHandlerAsync;
import org.activityinfo.legacy.shared.impl.ExecutionContext;
import org.activityinfo.legacy.shared.impl.Tables;

public class UpdateReportSubscriptionHandler implements CommandHandlerAsync<UpdateReportSubscription, VoidResult> {

    @Override
    public void execute(final UpdateReportSubscription command,
                        final ExecutionContext context,
                        final AsyncCallback<VoidResult> callback) {

        SqlUpdate update = SqlUpdate.update(Tables.REPORT_SUBSCRIPTION)
                                    .valueIfNotNull("dashboard", command.getPinnedToDashboard())
                                    .valueIfNotNull("emailday", command.getEmailDay())
                                    .where("reportId", command.getReportId())
                                    .where("userId", context.getUser().getId());

        if (command.getEmailDelivery() != null) {
            update.value("emaildelivery", command.getEmailDelivery().name());
        }

        if (update.isEmpty()) {
            callback.onSuccess(null);
        } else {
            update.execute(context.getTransaction(), new SqlResultCallback() {

                @Override
                public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                    if (results.getRowsAffected() != 0) {
                        // successfully updated
                        callback.onSuccess(null);
                    } else {
                        // need to insert new record

                        SqlInsert.insertInto(Tables.REPORT_SUBSCRIPTION)
                                 .value("dashboard",
                                         command.getPinnedToDashboard() != null && command.getPinnedToDashboard())
                                 .value("subscribed", false)
                                 .value("userId", context.getUser().getId())
                                 .value("reportId", command.getReportId())
                                 .execute(context.getTransaction(), new AsyncCallback<Integer>() {

                                     @Override
                                     public void onSuccess(Integer result) {
                                         callback.onSuccess(null);
                                     }

                                     @Override
                                     public void onFailure(Throwable caught) {
                                         callback.onFailure(caught);
                                     }
                                 });

                    }

                }
            });
        }
    }

}
