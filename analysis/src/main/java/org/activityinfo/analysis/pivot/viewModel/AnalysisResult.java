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
package org.activityinfo.analysis.pivot.viewModel;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.FormSource;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of the analysis
 */
public class AnalysisResult {

    private DimensionSet dimensionSet;
    private List<Point> points = new ArrayList<>();
    private EffectiveModel effectiveModel;

    public AnalysisResult(EffectiveModel effectiveModel, List<MeasureResultSet> measureSets) {
        this.effectiveModel = effectiveModel;
        if (!measureSets.isEmpty()) {
            dimensionSet = measureSets.get(0).getDimensions();
        } else {
            dimensionSet = new DimensionSet();
        }
        for (MeasureResultSet measureSet : measureSets) {
            points.addAll(measureSet.getPoints());
        }
    }

    public EffectiveModel getEffectiveModel() {
        return effectiveModel;
    }

    public DimensionSet getDimensionSet() {
        return dimensionSet;
    }

    public List<Point> getPoints() {
        return points;
    }

    public static Observable<AnalysisResult> compute(FormSource formSource, EffectiveModel effectiveModel) {

        List<Observable<MeasureResultSet>> points = new ArrayList<>();

        for (EffectiveMeasure measure : effectiveModel.getMeasures()) {
            points.add(computePoints(formSource, measure));
        }

        return Observable.flatten(points).transform(resultSets -> new AnalysisResult(effectiveModel, resultSets));
    }

    private static Observable<MeasureResultSet> computePoints(FormSource formSource, EffectiveMeasure measure) {

        QueryModel queryModel = new QueryModel(measure.getFormId());
        queryModel.selectExpr(measure.getModel().getFormula()).as("value");

        for (EffectiveMapping dim : measure.getDimensions()) {
            queryModel.addColumns(dim.getRequiredColumns());
        }

        Observable<ColumnSet> columnSet = formSource.query(queryModel);
        Observable<MeasureResultSet> resultSet = columnSet.transform(columns -> {
            MeasureResultBuilder builder = new MeasureResultBuilder(measure, columns);
            builder.execute();
            return builder.getResult();
        });

        return resultSet;
    }


}
