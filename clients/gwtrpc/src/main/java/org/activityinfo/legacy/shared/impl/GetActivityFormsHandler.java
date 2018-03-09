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
package org.activityinfo.legacy.shared.impl;

import com.bedatadriven.rebar.sql.client.SqlResultCallback;
import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.common.base.Function;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.ActivityFormResults;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.promise.Promise;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetActivityFormsHandler implements CommandHandlerAsync<GetActivityForms, ActivityFormResults> {

    private static final Logger LOGGER = Logger.getLogger(GetActivityFormsHandler.class.getName());

    @Override
    public void execute(GetActivityForms command, final ExecutionContext context, final AsyncCallback<ActivityFormResults> callback) {


        composeQuery(command.getFilter())
        .execute(context.getTransaction(), new SqlResultCallback() {
            @Override
            public void onSuccess(SqlTransaction tx, final SqlResultSet results) {
                context.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(SchemaDTO schema) {
                        LOGGER.log(Level.INFO, "Forms matching filter: " + results.getRows().size());

                        final List<Promise<ActivityFormDTO>> pending = new ArrayList<>();

                        for (SqlResultSetRow row : results.getRows()) {
                            int activityId = row.getInt("activityId");
                            boolean visible = (schema.getActivityById(activityId) != null);
                            if(visible) {
                                pending.add(fetchForm(context, activityId));
                            }
                        }

                        LOGGER.log(Level.INFO, "Forms pending: " + pending.size());

                        Promise.waitAll(pending).then(new Function<Void, ActivityFormResults>() {
                            @Nullable
                            @Override
                            public ActivityFormResults apply(@Nullable Void aVoid) {
                                LOGGER.log(Level.INFO, "Form loading completed.");

                                List<ActivityFormDTO> forms = new ArrayList<>();
                                for (Promise<ActivityFormDTO> pendingForm : pending) {
                                    forms.add(pendingForm.get());
                                }

                                return new ActivityFormResults(forms);
                            }
                        }).then(callback);
                    }
                });
            }

        });
    }

    private SqlQuery composeQuery(Filter filter) {
        if(filter.isRestricted(DimensionType.Indicator)) {
            return SqlQuery.selectDistinct()
                    .appendColumn("i.activityId")
                    .from(Tables.INDICATOR, "i")
                    .leftJoin(Tables.ACTIVITY, "a").on("a.activityId=i.activityId")
                    .where("indicatorId").in(filter.getRestrictions(DimensionType.Indicator))
                    .whereTrue("i.dateDeleted is NULL")
                    .whereTrue("a.dateDeleted is NULL");

        } else if(filter.isRestricted(DimensionType.Activity)) {
            return SqlQuery.selectDistinct()
                    .appendColumn("activityId")
                    .from(Tables.ACTIVITY)
                    .whereTrue("dateDeleted is NULL")
                    .where("activityId").in(filter.getRestrictions(DimensionType.Activity));

        } else if(filter.isRestricted(DimensionType.Database)) {
            return SqlQuery.selectDistinct()
                    .appendColumn("activityId")
                    .from(Tables.ACTIVITY)
                    .whereTrue("dateDeleted is NULL")
                    .where("databaseId").in(filter.getRestrictions(DimensionType.Database));
        } else {

            LOGGER.warning("No restrictions specified, returning empty set. Filter = " + filter);

            return SqlQuery.select("activityId").from(Tables.ACTIVITY).whereTrue("0=1");
        }

    }

    private Promise<ActivityFormDTO> fetchForm(ExecutionContext context, int activityId) {
        final Promise<ActivityFormDTO> form = new Promise<>();
        context.execute(new GetActivityForm(activityId), new AsyncCallback<ActivityFormDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                form.reject(caught);
            }

            @Override
            public void onSuccess(ActivityFormDTO result) {
                form.resolve(result);
            }
        });
        return form;
    }
}
