package org.activityinfo.legacy.shared.impl;

import com.bedatadriven.rebar.sql.client.SqlResultCallback;
import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.query.SqlInsert;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.bedatadriven.rebar.sql.client.query.SqlUpdate;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.client.type.DateUtilGWTImpl;
import org.activityinfo.legacy.shared.command.Month;
import org.activityinfo.legacy.shared.command.UpdateMonthlyReports;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.promise.Promise;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UpdateMonthlyReportsAsync implements CommandHandlerAsync<UpdateMonthlyReports, VoidResult> {
    @Override
    public void execute(final UpdateMonthlyReports command, final ExecutionContext context, AsyncCallback<VoidResult> callback) {

        queryPeriodMap(command, context).join(new Function<Map<Month, Integer>, Promise<VoidResult>>() {
            @Override
            public Promise<VoidResult> apply(Map<Month, Integer> input) {
                return executeUpdates(context.getTransaction(), command, input);
            }
        }).then(callback);
    }

    private Promise<Map<Month, Integer>> queryPeriodMap(UpdateMonthlyReports command, ExecutionContext context) {
        final Promise<Map<Month, Integer>> promise = new Promise<>();

        SqlQuery.select("reportingPeriodId", "Date2")
            .from(Tables.REPORTING_PERIOD, "rp")
            .where("siteId").equalTo(command.getSiteId())
            .execute(context.getTransaction(), new SqlResultCallback() {
                @Override
                public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                    Map<Month, Integer> periodMap = Maps.newHashMap();
                    for(SqlResultSetRow row : results.getRows()) {
                        Date endDate = row.getDate("Date2");
                        Month month = Month.of(endDate);
                        periodMap.put(month, row.getInt("reportingPeriodId"));
                    }
                    promise.resolve(periodMap);
                }
            });
        return promise;
    }

    private Promise<VoidResult> executeUpdates(SqlTransaction tx, UpdateMonthlyReports command, Map<Month, Integer> periodMap) {

        KeyGenerator generator = new KeyGenerator();

        List<Promise<Void>> pendingUpdates = new ArrayList<>();

        for (UpdateMonthlyReports.Change change : command.getChanges()) {

            Integer periodId = periodMap.get(change.getMonth());
            if(periodId == null) {
                periodId = generator.generateInt();
                periodMap.put(change.getMonth(), periodId);

                pendingUpdates.add(insertPeriod(tx, command.getSiteId(), periodId, change.getMonth()));
            }

            pendingUpdates.add(deleteValue(tx, periodId, change.getIndicatorId()));
            if(change.getValue() != null) {
                pendingUpdates.add(insertValue(tx, periodId, change.getIndicatorId(), change.getValue()));
            }
        }

        return Promise.waitAll(pendingUpdates).then(Functions.<VoidResult>constant(null));
    }


    private Promise<Void> deleteValue(SqlTransaction tx, Integer periodId, int indicatorId) {
        return executeUpdate(tx, SqlUpdate.delete(Tables.INDICATOR_VALUE)
            .where("reportingPeriodId", periodId)
            .where("indicatorId", indicatorId));
    }


    private Promise<Void> insertValue(SqlTransaction tx, Integer periodId, int indicatorId, Double value) {
        return executeInsert(tx, SqlInsert.insertInto(Tables.INDICATOR_VALUE)
            .value("reportingPeriodId", periodId)
            .value("indicatorId", indicatorId)
            .value("value", value));
    }


    private Promise<Void> insertPeriod(SqlTransaction tx, int siteId, Integer periodId, Month month) {
        DateRange range = DateUtilGWTImpl.INSTANCE.monthRange(month);

        SqlInsert query = SqlInsert.insertInto(Tables.REPORTING_PERIOD)
            .value("ReportingPeriodId", periodId)
            .value("SiteId", siteId)
            .value("Date1", range.getMinLocalDate())
            .value("Date2", range.getMaxLocalDate())
            .value("DateCreated", new Date())
            .value("DateEdited", new Date());

        return executeInsert(tx, query);
    }



    private Promise<Void> executeUpdate(SqlTransaction tx, SqlUpdate update) {
        final Promise<Void> promise = new Promise<>();
        update.execute(tx, new SqlResultCallback() {
            @Override
            public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                promise.resolve(null);
            }
        });
        return promise;
    }


    private Promise<Void> executeInsert(SqlTransaction tx, SqlInsert query) {
        final Promise<Void> promise = new Promise<>();
        query
            .execute(tx, new AsyncCallback<Integer>() {
                @Override
                public void onFailure(Throwable caught) {
                    promise.reject(caught);
                }

                @Override
                public void onSuccess(Integer result) {
                    promise.resolve(null);
                }
            });

        return promise;
    }

}
