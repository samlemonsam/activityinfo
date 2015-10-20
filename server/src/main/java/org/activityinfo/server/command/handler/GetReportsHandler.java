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

import com.bedatadriven.rebar.sql.client.*;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.GetReports;
import org.activityinfo.legacy.shared.command.result.ReportsResult;
import org.activityinfo.legacy.shared.impl.CommandHandlerAsync;
import org.activityinfo.legacy.shared.impl.ExecutionContext;
import org.activityinfo.legacy.shared.impl.Tables;
import org.activityinfo.legacy.shared.model.ReportMetadataDTO;
import org.activityinfo.legacy.shared.reports.model.EmailDelivery;
import org.activityinfo.promise.Promise;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Alex Bertram
 * @see org.activityinfo.legacy.shared.command.GetReports
 */
public class GetReportsHandler implements CommandHandlerAsync<GetReports, ReportsResult> {

    private static final Logger LOGGER = Logger.getLogger(GetReportsHandler.class.getName());

    @Override
    public void execute(final GetReports command,
                        final ExecutionContext context,
                        final AsyncCallback<ReportsResult> callback) {

        // note that we are excluding reports with a null title-- these
        // reports have not yet been explicitly saved by the user

        new Builder(context, callback).build();
    }

    private static class Builder {

        private final Map<Integer, ReportMetadataDTO> mySubscriptions = Maps.newHashMap();
        private final Map<Integer, Boolean> visibility = Maps.newHashMap();
        private final Set<Integer> myDatabases = Sets.newHashSet();
        private final List<ReportMetadataDTO> reports = Lists.newArrayList();

        private final Stopwatch stopwatch = Stopwatch.createStarted();

        private final ExecutionContext context;
        private final AsyncCallback<ReportsResult> callback;


        public Builder(ExecutionContext context, AsyncCallback<ReportsResult> callback) {
            this.context = context;
            this.callback = callback;
        }

        public void build() {
            List<Promise<Void>> tasks = Lists.newArrayList();
            tasks.add(loadMySubscriptions());
            tasks.add(loadMyDatabases());

            Promise.waitAll(tasks).join(new Function<Void, Promise<Void>>() {
                @Override
                public Promise<Void> apply(Void input) {
                    final Promise promise = new Promise();
                    SqlQuery.select()
                            .appendColumn("reportid")
                            .appendColumn("defaultDashboard")
                            .from(Tables.REPORT_VISIBILITY, "v")
                            .where("v.databaseId")
                            .in(myDatabases)
                            .execute(context.getTransaction(), new SqlResultCallback() {
                                @Override
                                public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                                    for (SqlResultSetRow row : results.getRows()) {
                                        int reportid = row.getInt("reportid");
                                        Boolean defaultDashboard = visibility.get(reportid);
                                        if (defaultDashboard != null && defaultDashboard) {
                                            continue;
                                        }
                                        if (!row.isNull("defaultDashboard")) {
                                            defaultDashboard = row.getBoolean("defaultDashboard");
                                        }

                                        visibility.put(reportid, defaultDashboard);
                                    }
                                    LOGGER.finest("visibilty map loaded in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
                                    promise.resolve(null);
                                }
                            });
                    return promise;
                }
            }).then(new Function<Void, Object>() {
                @Override
                public Object apply(Void input) {
                    loadReports();
                    return null;
                }
            });
        }

        private String sharedInString() {
            String s = "";

            Set<Integer> set = new TreeSet<>(visibility.keySet());
            int size = set.size();
            int i = 0;
            for (Integer key : set) {
                s = s + key;
                i++;

                if (i != size) {
                    s = s + ",";
                }
            }
            return s;
        }

        private void loadReports() {
            SqlQuery query = SqlQuery.select()
                    .appendColumn("r.reportTemplateId", "reportId")
                    .appendColumn("r.title", "title")
                    .appendColumn("r.ownerUserId", "ownerUserId")
                    .appendColumn("o.name", "ownerName")
                    .from(Tables.REPORT_TEMPLATE, "r")
                    .leftJoin(Tables.USER_LOGIN, "o")
                    .on("o.userid=r.ownerUserId");
            // build where clause manually to ensure proper grouping of and/or

            query.whereTrue("r.title is not null AND (r.ownerUserId=" + context.getUser().getId() +
                    " OR r.reportTemplateId in (" + sharedInString() + "))");


            LOGGER.info("Reports query: " + query.sql());

            query.execute(context.getTransaction(), new SqlResultCallback() {
                @Override
                public void onSuccess(final SqlTransaction tx, final SqlResultSet results) {
                    LOGGER.finest("Query fetched in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

                    for (SqlResultSetRow row : results.getRows()) {
                        ReportMetadataDTO dto = new ReportMetadataDTO();
                        dto.setId(row.getInt("reportId"));
                        dto.setAmOwner(row.getInt("ownerUserId") == context.getUser().getId());
                        dto.setOwnerName(row.getString("ownerName"));
                        dto.setTitle(row.getString("title"));
                        dto.setEditAllowed(dto.getAmOwner());

                        if (row.isNull("dashboard")) { // todo
                            // inherited from database-wide visibility
                            dto.setDashboard(!row.isNull("defaultDashboard") && row.getBoolean("defaultDashboard"));
                        }

                        reports.add(dto);
                    }

                    LOGGER.finest("Parsed and result is returned in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
                    callback.onSuccess(new ReportsResult(reports));
                }
            });
        }

        private Promise<Void> loadMyDatabases() {
            final Promise<Void> promise = new Promise<>();
            SqlQuery.selectSingle("d.databaseid")
                    .from(Tables.USER_DATABASE, "d")
                    .leftJoin(SqlQuery.selectAll()
                            .from(Tables.USER_PERMISSION)
                            .where("userpermission.UserId")
                            .equalTo(context.getUser().getId()), "p")
                    .on("p.DatabaseId = d.DatabaseId")
                    .where("d.ownerUserId")
                    .equalTo(context.getUser().getUserId())
                    .or("p.AllowView")
                    .equalTo(1)
                    .execute(context.getTransaction(), new SqlResultCallback() {
                        @Override
                        public boolean onFailure(SqlException e) {
                            promise.reject(e);
                            return super.onFailure(e);
                        }

                        @Override
                        public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                            for (SqlResultSetRow row : results.getRows()) {
                                myDatabases.add(row.getInt("databaseid"));
                            }
                            LOGGER.finest("myDatabases loaded in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
                            promise.resolve(null);
                        }
                    });
            return promise;
        }

        private Promise<Void> loadMySubscriptions() {
            final Promise<Void> promise = new Promise<>();
            SqlQuery.select()
                    .appendColumn("dashboard")
                    .appendColumn("emaildelivery")
                    .appendColumn("emailday")
                    .appendColumn("reportid")
                    .from(Tables.REPORT_SUBSCRIPTION)
                    .where("userId")
                    .equalTo(context.getUser().getUserId()).execute(context.getTransaction(), new SqlResultCallback() {
                @Override
                public boolean onFailure(SqlException e) {
                    promise.reject(e);
                    return super.onFailure(e);
                }

                @Override
                public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                    for (SqlResultSetRow row : results.getRows()) {
                        int reportId = row.getInt("reportid");

                        ReportMetadataDTO dto = new ReportMetadataDTO();
                        dto.setId(reportId);

                        if (!row.isNull("emaildelivery")) {
                            dto.setEmailDelivery(EmailDelivery.valueOf(row.getString("emaildelivery")));
                        }
                        if (row.isNull("emailday")) {
                            dto.setDay(1);
                        } else {
                            dto.setDay(row.getInt("emailday"));
                        }

                        if (!row.isNull("dashboard")) {
                            dto.setDashboard(row.getBoolean("dashboard"));
                        }

                        mySubscriptions.put(reportId, dto);
                    }
                    LOGGER.finest("mySubscription loaded in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
                    promise.resolve(null);
                }
            });
            return promise;
        }
    }


}

