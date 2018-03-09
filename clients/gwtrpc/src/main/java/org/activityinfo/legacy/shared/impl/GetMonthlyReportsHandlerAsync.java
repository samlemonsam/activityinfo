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
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.GetMonthlyReports;
import org.activityinfo.legacy.shared.command.result.MonthlyReportResult;
import org.activityinfo.legacy.shared.model.IndicatorRowDTO;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.promise.Promise;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetMonthlyReportsHandlerAsync implements CommandHandlerAsync<GetMonthlyReports, MonthlyReportResult> {
    @Override
    public void execute(final GetMonthlyReports command, final ExecutionContext context, AsyncCallback<MonthlyReportResult> callback) {


        final Promise<SqlResultSet> indicators = queryIndicators(command, context);
        final Promise<SqlResultSet> periods = queryPeriods(command, context);

        Promise.waitAll(indicators, periods).then(new Function<Void, MonthlyReportResult>() {
            @Nullable
            @Override
            public MonthlyReportResult apply(@Nullable Void input) {

                Map<Integer, IndicatorRowDTO> indicatorMap = new HashMap<Integer, IndicatorRowDTO>();
                List<IndicatorRowDTO> rows = Lists.newArrayList();

                for (SqlResultSetRow indicatorRow : indicators.get().getRows()) {

                    IndicatorRowDTO dto = new IndicatorRowDTO();
                    dto.setIndicatorId(indicatorRow.getInt("indicatorId"));
                    dto.setSiteId(command.getSiteId());
                    dto.setIndicatorName(indicatorRow.getString("indicatorName"));
                    dto.setCategory(indicatorRow.getString("category"));
                    dto.setActivityName(indicatorRow.getString("activityName"));
                    dto.setActivityId(indicatorRow.getInt("activityId"));

                    indicatorMap.put(dto.getIndicatorId(), dto);
                    rows.add(dto);
                }

                for (SqlResultSetRow period : periods.get().getRows()) {
                    Date endDate = period.getDate("Date2");
                    Month month = Month.of(endDate);

                    if (month.compareTo(command.getStartMonth()) >= 0 &&
                        month.compareTo(command.getEndMonth()) <= 0) {

                        IndicatorRowDTO row = indicatorMap.get(period.getInt("indicatorId"));
                        if (row != null) {
                            row.setValue(month, period.getDouble("value"));
                        }
                    }
                }

                return new MonthlyReportResult(rows);
            }
        }).then(callback);
    }

    private Promise<SqlResultSet> queryPeriods(final GetMonthlyReports command, final ExecutionContext context) {

        final Promise<SqlResultSet> result = new Promise<>();

        SqlQuery.select("rp.Date1", "rp.Date2", "v.indicatorId", "v.value")
            .from(Tables.REPORTING_PERIOD, "rp")
            .leftJoin(Tables.INDICATOR_VALUE, "v").on("rp.ReportingPeriodId=v.ReportingPeriodId")
            .where("rp.SiteId").equalTo(command.getSiteId())
            .execute(context.getTransaction(), new SqlResultCallback() {
                @Override
                public void onSuccess(SqlTransaction tx, SqlResultSet periodResults) {
                    result.resolve(periodResults);
                }
            });

        return result;
    }

    private Promise<SqlResultSet> queryIndicators(final GetMonthlyReports command, ExecutionContext context) {

        final Promise<SqlResultSet> promise = new Promise<>();

        SqlQuery activityQuery = SqlQuery.select("activityId")
            .from(Tables.SITE)
            .where("siteId").equalTo(command.getSiteId());

        SqlQuery.select()
            .appendColumn("i.indicatorId", "indicatorId")
            .appendColumn("i.name", "indicatorName")
            .appendColumn("i.category", "category")
            .appendColumn("a.name", "activityName")
            .appendColumn("a.activityId", "activityId")
            .from(Tables.INDICATOR, "i")
            .leftJoin(Tables.ACTIVITY, "a").on("i.activityId=a.activityId")
            .where("a.activityId").in(activityQuery)
            .orderBy("i.sortOrder")

            .execute(context.getTransaction(), new SqlResultCallback() {
                @Override
                public void onSuccess(SqlTransaction tx, SqlResultSet indicatorResults) {

                    promise.resolve(indicatorResults);
                }
            });

        return promise;
    }
}
