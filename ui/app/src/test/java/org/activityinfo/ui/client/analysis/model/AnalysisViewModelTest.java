package org.activityinfo.ui.client.analysis.model;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
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
                point(199, "Male"),
                point(212, "Female")));
    }

    @Test
    public void dimensionsWithTotal() {

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(surveyCount());
        model.getDimensions().add(genderDimension().setTotalIncluded(true));

        assertThat(points(model), containsInAnyOrder(
                point(199, "Male"),
                point(212, "Female"),
                point(199+212, "Total")));
    }

    @Test
    public void twoDimensions() {

        dumpQuery(Survey.FORM_ID, "Gender", "Married", "Age");

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(surveyCount());
        model.getDimensions().add(genderDimension());
        model.getDimensions().add(marriedDimension());

        assertThat(points(model), containsInAnyOrder(
                point(88, "Male", "Married"),
                point(56, "Male", "Single"),
                point(92, "Female", "Married"),
                point(64, "Female", "Single")));
    }

    @Test
    public void twoDimensionsWithTotals() {

        dumpQuery(Survey.FORM_ID, "Gender", "Married", "Age");

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(surveyCount());
        model.getDimensions().add(genderDimension().setTotalIncluded(true));
        model.getDimensions().add(marriedDimension());

        assertThat(points(model), containsInAnyOrder(
                point(88,    "Male",   "Married"),
                point(56,    "Male",   "Single"),
                point(92,    "Female", "Married"),
                point(64,    "Female", "Single"),
                point(88+92, "Total",  "Married"),
                point(56+64, "Total",  "Single")));
    }

    @Test
    public void twoDimensionsWithBothTotals() {

        dumpQuery(Survey.FORM_ID, "Gender", "Married", "Age");

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(surveyCount());
        model.getDimensions().add(genderDimension().setTotalIncluded(true));
        model.getDimensions().add(marriedDimension().setTotalIncluded(true));

        assertThat(points(model), containsInAnyOrder(
                point(88,    "Male",   "Married"),
                point(56,    "Male",   "Single"),
                point(92,    "Female", "Married"),
                point(64,    "Female", "Single"),
                point(88+92, "Total",  "Married"),
                point(56+64, "Total",  "Single"),
                point(88+56, "Male",   "Total"),
                point(92+64, "Female", "Total"),
                point(300,   "Total",  "Total")));
    }

    @Test
    public void twoDimensionsWithMediansAndTotals() {

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(medianAge());
        model.getDimensions().add(genderDimension().setTotalIncluded(true));
        model.getDimensions().add(marriedDimension().setTotalIncluded(true));

        assertThat(points(model), containsInAnyOrder(
                point(52.5, "Male",   "Married"),
                point(61.5, "Male",   "Single"),
                point(56.0, "Female", "Married"),
                point(52.0, "Female", "Single"),
                point(63.0, "Total",  "Married"),
                point(50.0, "Total",  "Single"),
                point(55.0, "Male",   "Total"),
                point(55.0, "Female", "Total"),
                point(55.0, "Total",  "Total")));
    }

    @Test
    public void median() {

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(medianAge());
        model.getDimensions().add(genderDimension());

        assertThat(points(model), containsInAnyOrder(
                point(54, "Male"),
                point(56, "Female")));
    }

    private DimensionModel genderDimension() {
        return new DimensionModel(ResourceId.generateCuid(),
                "Gender",
                new DimensionMapping(new SymbolExpr("Gender")));
    }

    private DimensionModel marriedDimension() {
        return new DimensionModel(ResourceId.generateCuid(),
                "Married",
                new DimensionMapping(new SymbolExpr("MARRIED")));
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

    private void dumpQuery(ResourceId formId, String... columns) {
        System.err.flush();
        QueryModel model = new QueryModel(formId);
        for (int i = 0; i < columns.length; i++) {
            model.selectExpr(columns[i]).as("c" + i);
        }
        ColumnSet columnSet = assertLoads(formStore.query(model));

        for (int i = 0; i < columns.length; i++) {
            System.out.print(columns[i]);
        }
        System.out.println();

        for (int i = 0; i < columnSet.getNumRows(); i++) {
            for (int j = 0; j < columns.length; j++) {
                ColumnView columnView = columnSet.getColumnView("c" + j);
                Object cell = columnView.get(i);
                String cells = "";
                if(cell != null) {
                    cells = cell.toString();
                }
                System.out.print(column(cells));
            }
            System.out.println();
        }
        System.out.flush();
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