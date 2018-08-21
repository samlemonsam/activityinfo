package org.activityinfo.analysis.pivot.viewModel;

import org.activityinfo.model.analysis.pivot.ImmutablePivotModel;
import org.activityinfo.model.analysis.pivot.PivotModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.FormSource;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Describes a PivotTable analysis and its results
 */
public class PivotViewModel {

    private static final Logger LOGGER = Logger.getLogger(PivotViewModel.class.getName());

    protected static final PivotModel EMPTY = ImmutablePivotModel.builder().build();

    private final FormSource formSource;

    private final Observable<FormForest> formForest;
    private final Observable<EffectiveModel> effectiveModel;
    private final Observable<AnalysisResult> resultTable;
    private final Observable<List<EffectiveDimension>> dimensions;
    private final Observable<PivotTable> pivotTable;

    public PivotViewModel(Observable<PivotModel> pivotModel, FormSource source) {
        this.formSource = source;
        this.formForest = pivotModel.join(m -> {
            LOGGER.info("Updating form forest");

            // Find unique list of forms involved in the analysis
            Set<ResourceId> forms = m.formIds();

            // Build a FormTree for each form
            List<Observable<FormTree>> trees = forms.stream().map(id -> formSource.getFormTree(id)).collect(Collectors.toList());

            // Combine into a FormForest
            return Observable.flatten(trees).transform(t -> new FormForest(t));
        });
        this.effectiveModel = Observable.transform(formForest, pivotModel, (ff, m) -> {
            LOGGER.info("Updating effective model");
            return new EffectiveModel(m, ff);
        });
        this.resultTable = effectiveModel.join( m -> {
            LOGGER.info("Updating result table");
            return AnalysisResult.compute(formSource, m);
        } );
        this.dimensions = effectiveModel.transform(em -> {
            LOGGER.info("Updating dimensions");
            return em.getDimensions();
        });
        this.pivotTable = resultTable.transform(t -> {
            LOGGER.info("Updating pivot table");
            return new PivotTable(t);
        });
    }

    public Observable<EffectiveModel> getEffectiveModel() {
        return effectiveModel;
    }

    public Observable<AnalysisResult> getResultTable() {
        return resultTable;
    }

    public Observable<List<EffectiveDimension>> getDimensions() {
        return dimensions;
    }

    public Observable<PivotTable> getPivotTable() {
        return pivotTable;
    }

    public Observable<FormForest> getFormForest() {
        return formForest;
    }
}
