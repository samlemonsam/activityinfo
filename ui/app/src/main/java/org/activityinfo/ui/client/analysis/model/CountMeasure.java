package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Function3;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Measure that resolves to a count of records in a given form.
 */
public class CountMeasure extends MeasureModel {

    private ResourceId formId;

    public CountMeasure(ResourceId formId, String label) {
        super(ResourceId.generateCuid(), label);
        this.formId = formId;
    }

    public ResourceId getFormId() {
        return formId;
    }

    @Override
    public Observable<List<DimensionSourceModel>> availableDimensions(FormStore store) {
        return store.getFormClass(formId).transform(formClass -> {
            List<DimensionSourceModel> sources = new ArrayList<>();
            sources.add(FormDimensionSource.INSTANCE);
            sources.addAll(FieldDimensionSource.sources(formClass));
            return sources;
        });
    }

    @Override
    public Observable<MeasureResultSet> compute(FormStore store, Observable<DimensionSet> dimensions) {
        QueryModel model = new QueryModel(formId);
        model.selectResourceId().as("id");

        Observable<ColumnSet> columnSet = store.query(model);
        Observable<FormClass> formClass = store.getFormClass(formId);

        return Observable.transform(formClass, columnSet, dimensions, new Function3<FormClass, ColumnSet, DimensionSet, MeasureResultSet>() {
            @Override
            public MeasureResultSet apply(FormClass formClass, ColumnSet columnSet, DimensionSet dimensions) {
                Point point = new Point(dimensions);
                point.setValue(columnSet.getNumRows());


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
