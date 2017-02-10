package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.StatefulList;
import org.activityinfo.ui.client.store.FormStore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Describes an analysis and its results
 */
public class AnalysisModel {

    private static final Logger LOGGER = Logger.getLogger(AnalysisModel.class.getName());

    private final FormStore formStore;

    private final StatefulList<MeasureModel> measures;
    private final StatefulList<DimensionModel> dimensions;

    private final Observable<FormForest> formForest;
    private final Observable<DimensionSet> dimensionSet;
    private final Observable<AnalysisResult> result;

    public AnalysisModel(FormStore formStore) {
        this.formStore = formStore;
        measures = new StatefulList<>();
        dimensions = new StatefulList<>();

        // The dimension sources are a function of the measures present in the analysis
        this.formForest = measures
                .flatMap(measure -> measure.getFormSet(formStore))
                .transform(FormForest::merge);


        // The dimension set is an unordered set of dimensions used in the analysis
        this.dimensionSet = dimensions
                .asObservable()
                .transform(dimList -> new DimensionSet(dimList));

        // The results are a function of the selected measures and dimensions
        this.result = measures
                .flatMap(measure -> measure.compute(formStore, dimensionSet))
                .transform(AnalysisResult::new);

    }

    public ObservableList<DimensionModel> getDimensions() {
        return dimensions;
    }

    public FormStore getFormStore() {
        return formStore;
    }

    /**
     * @return the list of measures present in this analysis.
     */
    public ObservableList<MeasureModel> getMeasures() {
        return measures;
    }

    public Observable<FormForest> getFormForest() {
        return formForest;
    }


    public void addMeasure(MeasureModel measure) {
        beforeChange();
        measures.add(measure);
    }

    public void addDimension(DimensionSourceModel dimensionSource) {
        beforeChange();
        DimensionModel newDimension = new DimensionModel(ResourceId.generateCuid(), dimensionSource);
        dimensions.add(newDimension);
    }

    public Observable<AnalysisResult> getResult() {
        return result;
    }


    public void removeDimension(String id) {
        dimensions.removeFirst(dim -> dim.getId().equals(id));
    }

    public void removeMeasure(String id) {
        measures.removeFirst(measure -> measure.getKey().equals(id));
    }

    public void beforeChange() {
        LOGGER.log(Level.INFO, "State: " + toJsonObject().toString());
    }

    public JsonObject toJsonObject() {

        JsonArray measures = new JsonArray();
        for (MeasureModel measureModel : this.measures.getList()) {
            measures.add(measureModel.toJsonObject());
        }

        JsonArray dimensions = new JsonArray();
        for (DimensionModel dimensionModel : this.dimensions.getList()) {
            dimensions.add(dimensionModel.toJsonObject());
        }

        JsonObject object = new JsonObject();
        object.add("measures", measures);
        object.add("dimensions", dimensions);
        return object;
    }

    public void loadFromJson(JsonObject object) {

        JsonArray measuresArray = object.getAsJsonArray("measures");
        List<MeasureModel> newMeasures = new ArrayList<>();


        JsonArray dimensionsArray = object.getAsJsonArray("dimensions");



    }


}
