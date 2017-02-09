package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.functions.SumFunction;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.BiFunction;
import org.activityinfo.store.query.shared.Aggregation;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.List;

/**
 * A measure calculated from a field value (or a formula) and then aggregated.
 */
public class FieldMeasure extends FormLevelMeasure {

    private ResourceId formId;
    private ExprNode expr;

    public FieldMeasure(String key, String label, ResourceId formId, ExprNode expr) {
        super(key, label, formId);
        this.formId = formId;
        this.expr = expr;
    }


    @Override
    public Observable<MeasureResultSet> compute(FormStore store, Observable<DimensionSet> dimensions) {
        return compute(store, formId, expr, dimensions);
    }

    @Override
    public JsonElement toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "field");
        object.addProperty("formId", "formId");
        object.addProperty("expr", expr.toString());
        return object;
    }

    private static class BaseData {
        private FormClass formClass;
        private DimensionSet dimensionSet;
        private ColumnSet columnSet;

        public BaseData(FormClass formClass, DimensionSet dimensionSet, ColumnSet columnSet) {
            this.formClass = formClass;
            this.dimensionSet = dimensionSet;
            this.columnSet = columnSet;
        }
    }

    public static Observable<MeasureResultSet> compute(FormStore store, ResourceId formId, ExprNode valueExpr, Observable<DimensionSet> dimensions) {

        Observable<FormClass> formClass = store.getFormClass(formId);
        Observable<BaseData> baseData = Observable.join(dimensions, formClass, new BiFunction<DimensionSet, FormClass, Observable<BaseData>>() {
            @Override
            public Observable<BaseData> apply(DimensionSet dimensionModels, FormClass formClass) {
                QueryModel queryModel = new QueryModel(formId);
                queryModel.selectExpr(valueExpr).as("value");
                for (DimensionModel dimension : dimensionModels) {
                    for (ColumnModel columnModel : dimension.getRequiredColumns()) {
                        queryModel.addColumn(columnModel);
                    }
                }
                return store.query(queryModel).transform(input -> new BaseData(formClass, dimensionModels, input));
            }
        });

        return baseData.transform(input -> {

            ColumnView value = input.columnSet.getColumnView("value");
            GroupMap groupMap = new GroupMap(input.dimensionSet, input.formClass, input.columnSet);

            // Build group/value pairs

            int numRows = input.columnSet.getNumRows();
            double valueArray[] = new double[numRows];
            int groupArray[] = new int[numRows];
            for (int i = 0; i < numRows; i++) {
                valueArray[i] = value.getDouble(i);
                groupArray[i] = groupMap.groupAt(i);
            }

            // Aggregate into groups
            int numGroups = groupMap.getGroupCount();
            double aggregatedValues[] = Aggregation.aggregate(SumFunction.INSTANCE,
                    groupArray,
                    valueArray,
                    numRows,
                    numGroups);

            // Add the points
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < numGroups; i++) {
                points.add(new Point(groupMap.getGroup(i), aggregatedValues[i]));
            }

            return new MeasureResultSet(input.dimensionSet, points);
        });
    }
}
