//package org.activityinfo.ui.client.analysis.model;
//
//import com.google.gson.JsonObject;
//import org.activityinfo.model.expr.ExprNode;
//import org.activityinfo.model.expr.ExprParser;
//import org.activityinfo.model.expr.functions.SumFunction;
//import org.activityinfo.model.form.FormClass;
//import org.activityinfo.model.query.ColumnModel;
//import org.activityinfo.model.query.ColumnSet;
//import org.activityinfo.model.query.ColumnView;
//import org.activityinfo.model.query.QueryModel;
//import org.activityinfo.model.resource.ResourceId;
//import org.activityinfo.observable.Observable;
//import org.activityinfo.observable.StatefulValue;
//import org.activityinfo.promise.Function3;
//import org.activityinfo.store.query.shared.Aggregation;
//import org.activityinfo.ui.client.analysis.viewModel.DimensionSet;
//import org.activityinfo.ui.client.analysis.viewModel.GroupMap;
//import org.activityinfo.ui.client.analysis.viewModel.Point;
//import org.activityinfo.ui.client.store.FormStore;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * A measure calculated from a field value (or a formula) and then aggregated.
// */
//public class FieldMeasure extends FormLevelMeasure {
//
//    private ResourceId formId;
//    private StatefulValue<String> expr = new StatefulValue<>();
//
//    public FieldMeasure(ResourceId formId, ExprNode expr) {
//        super(formId);
//        this.formId = formId;
//        this.expr.updateValue(expr.asExpression());
//    }
//
//
//    @Override
//    public Observable<MeasureResultSet> compute(FormStore store, Observable<DimensionSet> dimensions, Observable<MeasureLabels> measureLabels) {
//        return expr.join(formula -> compute(store, formId, ExprParser.parse(expr.get()), measureLabels, dimensions));
//    }
//
//    public void updateFormula(String formula) {
//        expr.updateValue(formula);
//    }
//
//    public Observable<String> getFormula() {
//        return expr;
//    }
//
//    @Override
//    public JsonObject toJsonObject() {
//        JsonObject object = new JsonObject();
//        object.addProperty("type", "field");
//        object.addProperty("formId", "formId");
//        object.addProperty("expr", expr.toString());
//        return object;
//    }
//
//    private static class BaseData {
//        private FormClass formClass;
//        private MeasureLabels measureLabels;
//        private DimensionSet dimensionSet;
//        private ColumnSet columnSet;
//
//        public BaseData(FormClass formClass, MeasureLabels measureLabels, DimensionSet dimensionSet, ColumnSet columnSet) {
//            this.formClass = formClass;
//            this.measureLabels = measureLabels;
//            this.dimensionSet = dimensionSet;
//            this.columnSet = columnSet;
//        }
//    }
//
//    public static Observable<MeasureResultSet> compute(FormStore store, ResourceId formId, ExprNode valueExpr, Observable<MeasureLabels> measureLabels, Observable<DimensionSet> dimensions) {
//
//        Observable<FormClass> formClass = store.getFormClass(formId);
//        Observable<BaseData> baseData = Observable.join(dimensions, measureLabels, formClass, new Function3<DimensionSet, MeasureLabels, FormClass, Observable<BaseData>>() {
//            @Override
//            public Observable<BaseData> apply(DimensionSet dimensionModels, MeasureLabels measureLabels, FormClass formClass) {
//                QueryModel queryModel = new QueryModel(formId);
//                queryModel.selectExpr(valueExpr).as("value");
//                for (DimensionModel dimension : dimensionModels) {
//                    for (ColumnModel columnModel : dimension.getRequiredColumns()) {
//                        queryModel.addColumn(columnModel);
//                    }
//                }
//                return store.query(queryModel).transform(input -> new BaseData(formClass, measureLabels, dimensionModels, input));
//            }
//        });
//
//        return baseData.transform(input -> {
//
//            ColumnView value = input.columnSet.getColumnView("value");
//            GroupMap groupMap = new GroupMap(input.measureLabels, input.dimensionSet, input.formClass, input.columnSet);
//
//            // Build group/value pairs
//            int numRows = input.columnSet.getNumRows();
//            double valueArray[] = new double[numRows];
//            int groupArray[] = new int[numRows];
//            for (int i = 0; i < numRows; i++) {
//                valueArray[i] = value.getDouble(i);
//                groupArray[i] = groupMap.groupAt(i);
//            }
//
//            // Aggregate into groups
//            int numGroups = groupMap.getGroupCount();
//            double aggregatedValues[] = Aggregation.aggregate(SumFunction.INSTANCE,
//                    groupArray,
//                    valueArray,
//                    numRows,
//                    numGroups);
//
//            // Add the points
//            List<Point> points = new ArrayList<>();
//            for (int i = 0; i < numGroups; i++) {
//                points.add(new Point(groupMap.getGroup(i), aggregatedValues[i]));
//            }
//
//            return new MeasureResultSet(input.dimensionSet, points);
//        });
//    }
//}
