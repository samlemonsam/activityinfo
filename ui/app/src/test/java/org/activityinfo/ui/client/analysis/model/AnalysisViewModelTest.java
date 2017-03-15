package org.activityinfo.ui.client.analysis.model;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTesting;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisResult;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.analysis.viewModel.Point;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AnalysisViewModelTest {

    private static final int COLUMN_LENGTH = 20;
    private static final String NA = null;
    private TestingFormStore formStore;

    @Before
    public void setup() {
        LocaleProxy.initialize();

        formStore = new TestingFormStore();

    }

    @Test
    public void testEmptyModel() {
        TestingFormStore formStore = new TestingFormStore();
        AnalysisViewModel model = new AnalysisViewModel(formStore);

        AnalysisResult result = assertLoads(model.getResultTable());
    }


    @Test
    public void testSimpleCount() {

        formStore.delayLoading();

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(surveyCount());

        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(model);

        Connection<AnalysisResult> result = ObservableTesting.connect(viewModel.getResultTable());
        result.assertLoading();

        formStore.loadAll();

        List<Point> points = result.assertLoaded().getPoints();

        assertThat(points, hasSize(1));
        assertThat(points.get(0).getValue(), equalTo((double)Survey.ROW_COUNT));
    }

    @Test
    public void dimensionsWithMissing() {

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(surveyCount());
        model.getDimensions().add(genderDimension());

        assertThat(points(model), containsInAnyOrder(
                point(197, "Male"),
                point(201, "Female"),
                point(138, NA)));
    }

    @Test
    public void median() {

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(medianAge());
        model.getDimensions().add(genderDimension());

        assertThat(points(model), containsInAnyOrder(
                point(54, "Male"),
                point(56, "Female"),
                point(64, NA)));
    }


    @Test
    @Ignore
    public void dimensionsWithoutMissing() {

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(medianAge());
        model.getDimensions().add(genderDimension());

        assertThat(points(model), containsInAnyOrder(
                point(197, "Male"),
                point(201, "Female")));
    }

    private DimensionModel genderDimension() {
        return new DimensionModel(ResourceId.generateCuid(),
                "Gender",
                new DimensionMapping(new SymbolExpr("Gender")));
    }

    private MeasureModel surveyCount() {
        return new MeasureModel(ResourceId.generateCuid(),
                "Count",
                Survey.FORM_ID, "1");
    }

    private MeasureModel medianAge() {
        return new MeasureModel(ResourceId.generateCuid(),
                "Age",
                Survey.FORM_ID, Survey.AGE_FIELD_ID.asString())
                .setAggregation("median");
    }


    private Point point(double value, String... dimensions) {
        return new Point(dimensions, value);
    }

    /**
     * Computes the result of the analysis and returns the array of points.
     */
    private List<Point> points(AnalysisModel model) {
        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(model);

        AnalysisResult result = assertLoads(viewModel.getResultTable());

        dump(result);

        return result.getPoints();
    }

    private void dump(AnalysisResult result) {

        for (DimensionModel dimensionModel : result.getDimensionSet()) {
            System.out.print(column(dimensionModel.getLabel()));
        }
        System.out.println(column("Value"));

        for (Point point : result.getPoints()) {
            for (int i = 0; i < result.getDimensionSet().getCount(); i++) {
                System.out.print(column(point.getDimension(i)));
            }
            System.out.println(column(Double.toString(point.getValue())));
        }
    }


    private String column(String s) {
        if(s == null) {
            s = "";
        }
        if(s.length() > COLUMN_LENGTH) {
            return s.substring(0, 20);
        } else {
            return Strings.padEnd(s, COLUMN_LENGTH, ' ');
        }
    }

    private <T> T assertLoads(Observable<T> result) {
        List<T> results = new ArrayList<>();
        result.subscribe(observable -> {
            if (observable.isLoaded()) {
                results.add(observable.get());
            }
        });
        if (results.isEmpty()) {
            throw new AssertionError("Observable did not load synchronously.");
        }
        return Iterables.getLast(results);
    }
}