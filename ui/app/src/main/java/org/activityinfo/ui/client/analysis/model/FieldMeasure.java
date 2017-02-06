package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Function3;
import org.activityinfo.ui.client.store.FormStore;

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
        QueryModel model = new QueryModel(formId);
        model.selectResourceId().as("id");
        model.selectExpr(expr).as("value");

        Observable<ColumnSet> columnSet = store.query(model);
        Observable<FormClass> formClass = store.getFormClass(formId);

        return Observable.transform(formClass, columnSet, dimensions, new Function3<FormClass, ColumnSet, DimensionSet, MeasureResultSet>() {
            @Override
            public MeasureResultSet apply(FormClass formClass, ColumnSet columnSet, DimensionSet dimensions) {

                ColumnView view = columnSet.getColumnView("value");
                double sum = 0;
                for (int i = 0; i < view.numRows(); i++) {
                    double value = view.getDouble(i);
                    if(!Double.isNaN(value)) {
                        sum += value;
                    }
                }

                Point point = new Point(dimensions);
                point.setValue(sum);

                for (int i = 0; i < dimensions.getCount(); i++) {
                    DimensionModel dim = dimensions.getDimension(i);
                    if (dim.getSourceModel() instanceof FormDimensionSource) {
                        point.setDimension(i, formClass.getLabel());
                    }
                }

                return new MeasureResultSet(dimensions, point);
            }
        });
    }
}
