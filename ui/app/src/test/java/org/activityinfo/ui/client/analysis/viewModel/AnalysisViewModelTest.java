package org.activityinfo.ui.client.analysis.viewModel;

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
import org.activityinfo.store.testing.IntakeForm;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.analysis.model.*;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
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

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .build();

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


        AnalysisModel model = ImmutableAnalysisModel.builder()
            .addMeasures(surveyCount())
            .addDimensions(genderDimension())
            .build();

        assertThat(points(model), containsInAnyOrder(
                point(199, "Male"),
                point(212, "Female")));
    }

    @Test
    public void dimensionsWithSeveralStatistics() {


        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(medianAge().withStatistics(Statistic.MIN, Statistic.MAX, Statistic.MEDIAN))
                .addDimensions(genderDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(15, Statistic.MIN, "Male"),
                point(15, Statistic.MIN, "Female"),
                point(98, Statistic.MAX, "Male"),
                point(98, Statistic.MAX, "Female"),
                point(56.5, Statistic.MEDIAN, "Male"),
                point(51.0, Statistic.MEDIAN, "Female")));
    }


    @Test
    public void dimensionsWithTotal() {

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension().withTotals(true))
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(199, "Male"),
                point(212, "Female"),
                point(199+212, "Total")));
    }

//    @Test
//    public void dimensionsWithTotalPercentages() {
//
//        AnalysisModel model = ImmutableAnalysisModel.builder()
//                .addMeasures(surveyCount().withStatistics(Statistic.PERCENTAGE))
//                .addDimensions(genderDimension().withTotals(true))
//                .build();
//
//        assertThat(points(model), containsInAnyOrder(
//                point(199, "Male"),
//                point(212, "Female"),
//                point(199+212, "Total")));
//    }

    @Test
    public void dimensionListItems() {

        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(
                ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .build());

        ImmutableDimensionModel genderDimension = genderDimension();

        // Add a new dimension
        viewModel.updateModel(viewModel.getModel().withDimension(genderDimension));
        assertThat(assertLoads(viewModel.getDimensionListItems()), hasSize(1));

        // Delete a non-existant dimension
        viewModel.updateModel(viewModel.getModel().withoutDimension("FOOOO"));
        assertThat(assertLoads(viewModel.getDimensionListItems()), hasSize(1));

        // Delete the gender dimension
        viewModel.updateModel(viewModel.getModel().withoutDimension(genderDimension.getId()));
        assertThat(assertLoads(viewModel.getDimensionListItems()), hasSize(0));
    }


    @Test
    public void twoDimensions() {

        dumpQuery(Survey.FORM_ID, "Gender", "Married", "Age");

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension())
                .addDimensions(marriedDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(88, "Male", "Married"),
                point(56, "Male", "Single"),
                point(92, "Female", "Married"),
                point(64, "Female", "Single")));
    }

    @Test
    public void twoDimensionsWithTotals() {

        dumpQuery(Survey.FORM_ID, "Gender", "Married", "Age");

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension().withTotals(true))
                .addDimensions(marriedDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(88,    "Male",   "Married"),
                point(92,    "Female", "Married"),
                point(88+92, "Total",  "Married"),
                point(56,    "Male",   "Single"),
                point(64,    "Female", "Single"),
                point(56+64, "Total",  "Single")));
    }



    @Test
    public void twoDimensionsWithBothTotals() {

        dumpQuery(Survey.FORM_ID, "Gender", "Married", "Age");

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension().withTotals(true))
                .addDimensions(marriedDimension().withTotals(true))
                .build();

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
    public void dateDimension() {
        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(intakeCaseCount())
                .addDimensions(caseYear())
                .addDimensions(caseQuarter().withTotals(true))
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(137,   "2016",   "Q1"),
                point(148,   "2016",   "Q2"),
                point(142,   "2016",   "Q3"),
                point(130,   "2016",   "Q4"),
                point(140,   "2017",   "Q1"),
                point(143,   "2017",   "Q2"),
                point(150,   "2017",   "Q3"),
                point(137,   "2017",   "Q4"),
                point(557,   "2016",   "Total"),
                point(570,   "2017",   "Total")));
    }

    @Test
    public void multiDimensions() {

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(intakeCaseCount())
                .addDimensions(caseYear())
                .addDimensions(nationality())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(411,   "2016",   "Palestinian"),
                point(422,   "2017",   "Palestinian"),
                point(138,   "2016",   "Jordanian"),
                point(144,   "2017",   "Jordanian"),
                point(71,    "2016",   "Syrian"),
                point(84,    "2017",   "Syrian")));
    }

    @Test
    public void severalMultiDimensions() {

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(intakeCaseCount())
                .addDimensions(caseYear())
                .addDimensions(nationality())
                .addDimensions(protectionProblem())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(227,   "2016",   "Palestinian", "Documents"),
                point(277,   "2016",   "Palestinian", "Access to Services"),
                point(250,   "2017",   "Palestinian", "Documents"),
                point(273,   "2017",   "Palestinian", "Access to Services"),
                point(76,    "2016",   "Jordanian",   "Documents"),
                point(88,    "2016",   "Jordanian",   "Access to Services"),
                point(85,    "2017",   "Jordanian",   "Documents"),
                point(90,    "2017",   "Jordanian",   "Access to Services"),
                point(36,    "2016",   "Syrian",      "Documents"),
                point(54,    "2016",   "Syrian",      "Access to Services"),
                point(49,    "2017",   "Syrian",      "Documents"),
                point(60,    "2017",   "Syrian",      "Access to Services")
        ));
    }

    @Ignore("WIP")
    @Test
    public void severalMultiDimensionsWithTotals() {

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(intakeCaseCount())
                .addDimensions(caseYear().withTotals(true))
                .addDimensions(nationality())
                .addDimensions(protectionProblem())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(227,   "2016",   "Palestinian", "Documents"),
                point(277,   "2016",   "Palestinian", "Access to Services"),
                point(250,   "2017",   "Palestinian", "Documents"),
                point(273,   "2017",   "Palestinian", "Access to Services"),
                point(76,    "2016",   "Jordanian",   "Documents"),
                point(88,    "2016",   "Jordanian",   "Access to Services"),
                point(85,    "2017",   "Jordanian",   "Documents"),
                point(90,    "2017",   "Jordanian",   "Access to Services"),
                point(36,    "2016",   "Syrian",      "Documents"),
                point(54,    "2016",   "Syrian",      "Access to Services"),
                point(49,    "2017",   "Syrian",      "Documents"),
                point(60,    "2017",   "Syrian",      "Access to Services"),

                point(477,   "Total",  "Palestinian", "Documents"),
                point(550,   "Total",  "Palestinian", "Access to Services"),
                point(161,   "Total",  "Jordanian",   "Documents"),
                point(178,   "Total",  "Jordanian",   "Access to Services"),
                point(85,    "Total",  "Syrian",      "Documents"),
                point(114,   "Total",  "Syrian",      "Access to Services")
        ));
    }


    @Test
    public void twoDimensionsWithMediansAndTotals() {

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(medianAge())
                .addDimensions(genderDimension().withTotals(true))
                .addDimensions(marriedDimension().withTotals(true))
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(52.5, Statistic.MEDIAN, "Male",   "Married"),
                point(61.5, Statistic.MEDIAN, "Male",   "Single"),
                point(56.0, Statistic.MEDIAN, "Female", "Married"),
                point(52.0, Statistic.MEDIAN, "Female", "Single"),
                point(63.0, Statistic.MEDIAN, "Total",  "Married"),
                point(50.0, Statistic.MEDIAN, "Total",  "Single"),
                point(55.0, Statistic.MEDIAN, "Male",   "Total"),
                point(55.0, Statistic.MEDIAN, "Female", "Total"),
                point(55.0, Statistic.MEDIAN, "Total",  "Total")));
    }

    @Test
    public void median() {

        //dumpQuery(Survey.FORM_ID, "Gender", "age");

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(medianAge())
                .addDimensions(genderDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(56.5, Statistic.MEDIAN, "Male"),
                point(51.0, Statistic.MEDIAN, "Female")));
    }

    private ImmutableDimensionModel genderDimension() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Gender")
                .addMappings(new DimensionMapping(new SymbolExpr("Gender")))
                .build();
    }


    private ImmutableDimensionModel marriedDimension() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Married")
                .addMappings(new DimensionMapping(new SymbolExpr("MARRIED")))
                .build();
    }

    public static ImmutableMeasureModel surveyCount() {
        return ImmutableMeasureModel.builder()
                .label("Count")
                .formId(Survey.FORM_ID)
                .formula("1")
                .build();
    }


    private MeasureModel intakeCaseCount() {
        return ImmutableMeasureModel.builder()
            .label("Count")
            .formId(IntakeForm.FORM_ID)
            .formula("1")
            .build();

    }
    private ImmutableDimensionModel caseYear() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Year")
                .addMappings(new DimensionMapping(IntakeForm.FORM_ID, IntakeForm.OPEN_DATE_FIELD_ID))
                .dateLevel(DateLevel.YEAR)
                .build();
    }


    private ImmutableDimensionModel caseQuarter() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Quarter")
                .addMappings(new DimensionMapping(IntakeForm.FORM_ID, IntakeForm.OPEN_DATE_FIELD_ID))
                .dateLevel(DateLevel.QUARTER)
                .build();
    }


    private ImmutableDimensionModel nationality() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Nationality")
                .addMappings(new DimensionMapping(IntakeForm.FORM_ID, IntakeForm.NATIONALITY_FIELD_ID))
                .build();
    }

    private ImmutableDimensionModel protectionProblem() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Problem")
                .addMappings(new DimensionMapping(IntakeForm.FORM_ID, IntakeForm.PROBLEM_FIELD_ID))
                .build();
    }

    private ImmutableMeasureModel medianAge() {
        return ImmutableMeasureModel.builder()
            .label("Age")
            .formId(Survey.FORM_ID)
            .formula(Survey.AGE_FIELD_ID.asString())
            .addStatistics(Statistic.MEDIAN)
            .build();
    }

    private TypeSafeMatcher<Point> point(double value, String... dimensions) {
        return point(value, Statistic.SUM, dimensions);
    }

    private TypeSafeMatcher<Point> point(double value, Statistic statistic, String... dimensions) {
        return new TypeSafeMatcher<Point>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(new Point(value, statistic, dimensions));
            }

            @Override
            protected boolean matchesSafely(Point point) {

                double absDiff = Math.abs(point.getValue() - value);
                if(absDiff >= 1.0) {
                    return false;
                }
                if(point.getStatistic() != statistic) {
                    return false;
                }

                for (int i = 0; i < dimensions.length; i++) {
                    if (!dimensions[i].equals(point.getDimension(i))) {
                        return false;
                    }
                }
                return true;
            }
        };
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
            System.out.print(column(columns[i]));
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
        System.out.print(column("Statistic"));
        System.out.println(column("Value"));

        for (Point point : result.getPoints()) {
            for (int i = 0; i < result.getDimensionSet().getCount(); i++) {
                System.out.print(column(point.getDimension(i)));
            }
            System.out.print(column(point.getStatistic().name()));
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