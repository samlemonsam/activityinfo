package org.activityinfo.legacy.shared.impl;

import com.bedatadriven.rebar.sql.client.SqlResultCallback;
import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.bedatadriven.rebar.sql.client.util.RowHandler;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.GetIndicators;
import org.activityinfo.legacy.shared.command.result.IndicatorResult;
import org.activityinfo.legacy.shared.model.IndicatorDTO;

import java.util.List;

public class GetIndicatorsHandler implements CommandHandlerAsync<GetIndicators, IndicatorResult> {
    @Override
    public void execute(GetIndicators command, ExecutionContext context, final AsyncCallback<IndicatorResult> callback) {
        SqlQuery.select(
                "i.indicatorId",
                "i.activityId",
                "i.name",
                "i.type",
                "i.expression",
                "i.skipExpression",
                "i.nameInExpression",
                "i.calculatedAutomatically",
                "i.category",
                "i.listHeader",
                "i.description",
                "i.aggregation",
                "i.units",
                "i.activityId",
                "i.sortOrder",
                "i.mandatory",
                "a.databaseId")
                .appendColumn("db.name", "databaseName")
                .from(Tables.INDICATOR)
                .leftJoin(Tables.ACTIVITY, "a").on("a.activityId=i.activityId")
                .leftJoin(Tables.USER_DATABASE, "db").on("a.databaseId=db.databaseId")
                .where("indicatorId").in(command.getIndicatorIds())
                .whereTrue("dateDeleted is null")
                .orderBy("SortOrder")
                .execute(context.getTransaction(), new SqlResultCallback() {

            @Override
            public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                List<IndicatorDTO> indicators = Lists.newArrayList();
                for(SqlResultSetRow row : results.getRows()) {
                    indicators.add(createDto(row));
                }
                callback.onSuccess(new IndicatorResult(indicators));
            }
        });
    }

    private IndicatorDTO createDto(SqlResultSetRow rs) {
        IndicatorDTO indicator = new IndicatorDTO();
        indicator.setId(rs.getInt("indicatorId"));
        indicator.setActivityId(rs.getInt("activityId"));
        indicator.setDatabaseId(rs.getInt("databaseId"));
        indicator.setDatabaseName(rs.getString("databaseName"));
        indicator.setName(rs.getString("name"));
        indicator.setTypeId(rs.getString("type"));
        indicator.setExpression(rs.getString("expression"));
        indicator.setSkipExpression(rs.getString("skipExpression"));
        indicator.setNameInExpression(rs.getString("nameInExpression"));
        indicator.setCalculatedAutomatically(rs.getBoolean("calculatedAutomatically"));
        indicator.setCategory(rs.getString("category"));
        indicator.setListHeader(rs.getString("listHeader"));
        indicator.setDescription(rs.getString("description"));
        indicator.setAggregation(rs.getInt("aggregation"));
        indicator.setUnits(rs.getString("units"));
        indicator.setMandatory(rs.getBoolean("mandatory"));
        indicator.setSortOrder(rs.getInt("sortOrder"));
        return indicator;
    }
}
