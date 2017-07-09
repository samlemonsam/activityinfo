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
import java.util.Arrays;
import java.util.List;

import static org.activityinfo.ui.client.analysis.viewModel.Point.TOTAL;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AnalysisViewModelTest {

    private static boolean DUMP_RAW_DATA = false;

    private static final int COLUMN_LENGTH = 20;
    private static final String NA = null;
    private TestingFormStore formStore;
    private Survey survey;
    private IntakeForm intakeForm;

    @Before
    public void setup() {
        LocaleProxy.initialize();

        formStore = new TestingFormStore();
        survey = formStore.getCatalog().getSurvey();
        intakeForm = formStore.getCatalog().getIntakeForm();
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
        assertThat(points.get(0).getValue(), equalTo((double) survey.getRowCount()));
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
    public void pivotDimensionsWithSeveralStatistics() {
        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(medianAge().withStatistics(Statistic.MIN, Statistic.MAX))
                .addDimensions(genderDimension())
                .build();

        assertThat(pivot(model), equalTo(table(
                "Gender   Female   Statistic   Min   15   ",
                "                              Max   98   ",
                "         Male     Statistic   Min   15   ",
                "                              Max   98   "
        )));
    }

    @Test
    public void pivotDimensionsWithSeveralStatisticsExplicitStatDim() {
        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(medianAge().withStatistics(Statistic.MIN, Statistic.MAX))
                .addDimensions(genderDimension())
                .addDimensions(statDimension().withAxis(Axis.COLUMN))
                .build();

        assertThat(pivot(model), equalTo(table(
                "                  Statistic         ",
                "                  Min         Max   ",
                "Gender   Female   15          98    ",
                "         Male     15          98    ")));
    }

    @Test
    public void pivotWithCustomTotalLabel() {
        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(medianAge().withStatistics(Statistic.MIN))
                .addDimensions(genderDimension().withTotals(true).withTotalLabel("ALL"))
                .build();

        assertThat(pivot(model), equalTo(table(
                "Gender   Female   15   ",
                "         Male     15   ",
                "         ALL      15   ")));
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
                point(199+212, TOTAL)));
    }

    @Test
    public void dimensionWithPercentages() {

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension().withPercentage(true))
                .build();

        assertThat(pivot(model), equalTo(
                table("Gender   Female   Statistic   Sum   212   ",
                      "                              %     52%   ",
                      "         Male     Statistic   Sum   199   ",
                      "                              %     48%   ")));
    }

    @Test
    public void dimensionWithPercentagesWithTotals() {

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension().withPercentage(true).withTotals(true))
                .addDimensions(statDimension().withAxis(Axis.COLUMN))
                .build();

        assertThat(pivot(model), equalTo(
                table("                  Statistic          ",
                      "                  Sum         %      ",
                      "Gender   Female   212         52%    ",
                      "         Male     199         48%    ",
                      "         Total    411         100%   ")));
    }

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

        dumpQuery(survey.getFormId(), "Gender", "Married", "Age");

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
    public void twoDimensionsWithPercentages() {

        dumpQuery(survey.getFormId(), "Gender", "Married", "Age");

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension()
                        .withPercentage(true)
                        .withAxis(Axis.ROW)
                        .withTotals(true))
                .addDimensions(marriedDimension()
                        .withAxis(Axis.COLUMN)
                        .withLabel("Civil Status")
                        .withTotals(true)
                        .withPercentage(true))
                .addDimensions(statDimension().withAxis(Axis.COLUMN))
                .build();

        pivot(model);

    }


    @Test
    public void twoDimensionsWithTotals() {

        dumpQuery(survey.getFormId(), "Gender", "Married", "Age");

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension().withTotals(true))
                .addDimensions(marriedDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(88,    "Male",   "Married"),
                point(92,    "Female", "Married"),
                point(88+92, TOTAL,    "Married"),
                point(56,    "Male",   "Single"),
                point(64,    "Female", "Single"),
                point(56+64, TOTAL,    "Single")));
    }

    @Test
    public void twoDimensionsPivotedInRows() {


        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension())
                .addDimensions(marriedDimension())
                .build();

        assertThat(pivot(model), equalTo(
                table("Gender   Female   Married   Married   92   ",
                      "                            Single    64   ",
                      "         Male     Married   Married   88   ",
                      "                            Single    56   ")));

    }

    @Test
    public void twoDimensionsPivoted() {
        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(surveyCount())
                .addDimensions(genderDimension())
                .addDimensions(marriedDimension().withAxis(Axis.COLUMN))
                .build();

        assertThat(pivot(model), equalTo(
                table("                  Married            ",
                      "                  Married   Single   ",
                      "Gender   Female   92        64       ",
                      "         Male     88        56       ")));

    }

    private String pivot(AnalysisModel model) {
        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(model);
        AnalysisResult analysisResult = assertLoads(viewModel.getResultTable());
        PivotTable table = new PivotTable(analysisResult);

        String text = PivotTableRenderer.render(table);

        System.out.println(text);

        return text;
    }

    private String table(String... rows) {
        StringBuilder s = new StringBuilder();
        for (String row : rows) {
            s.append(row).append('\n');
        }
        return s.toString();
    }

    @Test
    public void twoDimensionsWithBothTotals() {

        dumpQuery(survey.getFormId(), "Gender", "Married", "Age");

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
                point(88+92, TOTAL,  "Married"),
                point(56+64, TOTAL,  "Single"),
                point(88+56, "Male",   TOTAL),
                point(92+64, "Female", TOTAL),
                point(300,   TOTAL,  TOTAL)));
    }

    @Test
    public void multipleMeasures() {
        AnalysisModel model = ImmutableAnalysisModel.builder()
            .addMeasures(intakeCaseCount().withLabel("Cases"))
            .addMeasures(numChildren().withLabel("Children"))
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(1127,   "Cases"),
            point(1525,   "Children")));
    }

    @Test
    public void multipleMeasuresAndDimensions() {
        AnalysisModel model = ImmutableAnalysisModel.builder()
            .addMeasures(intakeCaseCount().withLabel("Cases"))
            .addMeasures(numChildren().withLabel("Children"))
            .addDimensions(this.caseYear())
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(557,       "2016",   "Cases"),
            point(570,       "2017",   "Cases")));
    }
    @Test
    public void multipleMeasuresAndDimensionsIncludeMissing() {
        AnalysisModel model = ImmutableAnalysisModel.builder()
            .addMeasures(intakeCaseCount().withLabel("Cases"))
            .addMeasures(numChildren().withLabel("Children"))
            .addDimensions(this.caseYear().withShowMissing(true))
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(557,       "2016",   "Cases"),
            point(570,       "2017",   "Cases"),
            point(1525,       "None",   "Children")));
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
                point(557,   "2016",   TOTAL),
                point(570,   "2017",   TOTAL)));
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

                point(477,   TOTAL,  "Palestinian", "Documents"),
                point(550,   TOTAL,  "Palestinian", "Access to Services"),
                point(161,   TOTAL,  "Jordanian",   "Documents"),
                point(178,   TOTAL,  "Jordanian",   "Access to Services"),
                point(85,    TOTAL,  "Syrian",      "Documents"),
                point(114,   TOTAL,  "Syrian",      "Access to Services")
        ));
    }

    @Test
    public void sort() {
        double[] array = new double[] {
                41.5, Double.NaN, Double.NaN, 3, 932535, 1, Double.NaN,
                -356, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN,
                -Double.MAX_VALUE, Double.MAX_VALUE};
        Arrays.sort(array);

        System.out.println(Arrays.toString(array));

        assertTrue(Arrays.equals(new double[] {
                 Double.NEGATIVE_INFINITY,
                -Double.MAX_VALUE,
                -356,
                1.0, 3.0, 41.5, 932535,
                Double.MAX_VALUE,
                Double.POSITIVE_INFINITY,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN }, array));
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
                point(63.0, Statistic.MEDIAN, TOTAL,  "Married"),
                point(50.0, Statistic.MEDIAN, TOTAL,  "Single"),
                point(55.0, Statistic.MEDIAN, "Male",   TOTAL),
                point(55.0, Statistic.MEDIAN, "Female", TOTAL),
                point(55.0, Statistic.MEDIAN, TOTAL,  TOTAL)));
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

    @Test
    public void medianWithMissing() {

        AnalysisModel model = ImmutableAnalysisModel.builder()
                .addMeasures(numChildren().withStatistics(Statistic.MEDIAN))
                .addDimensions(genderDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(3.0, Statistic.MEDIAN, "Male"),
                point(4.0, Statistic.MEDIAN, "Female")));
    }

    private ImmutableDimensionModel genderDimension() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Gender")
                .showMissing(false)
                .addMappings(new DimensionMapping(new SymbolExpr("Gender")))
                .build();
    }

    private ImmutableDimensionModel statDimension() {
        return ImmutableDimensionModel.builder()
                .id(DimensionModel.STATISTIC_ID)
                .label("Statistic")
                .build();
    }

    private ImmutableDimensionModel marriedDimension() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Married")
                .addMappings(new DimensionMapping(new SymbolExpr("MARRIED")))
                .showMissing(false)
                .build();
    }

    public ImmutableMeasureModel surveyCount() {
        return ImmutableMeasureModel.builder()
                .label("Count")
                .formId(survey.getFormId())
                .formula("1")
                .build();
    }

    private ImmutableMeasureModel intakeCaseCount() {
        return ImmutableMeasureModel.builder()
            .label("Count")
            .formId(intakeForm.getFormId())
            .formula("1")
            .build();

    }
    private ImmutableDimensionModel caseYear() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Year")
                .showMissing(false)
                .addMappings(new DimensionMapping(intakeForm.getFormId(), intakeForm.getOpenDateFieldId()))
                .dateLevel(DateLevel.YEAR)
                .build();
    }


    private ImmutableDimensionModel caseQuarter() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Quarter")
                .showMissing(false)
                .addMappings(new DimensionMapping(intakeForm.getFormId(), intakeForm.getOpenDateFieldId()))
                .dateLevel(DateLevel.QUARTER)
                .build();
    }


    private ImmutableDimensionModel nationality() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Nationality")
                .showMissing(false)
                .addMappings(new DimensionMapping(intakeForm.getFormId(), intakeForm.getNationalityFieldId()))
                .build();
    }

    private ImmutableDimensionModel protectionProblem() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Problem")
                .showMissing(false)
                .addMappings(new DimensionMapping(intakeForm.getFormId(), intakeForm.getProblemFieldId()))
                .build();
    }

    private ImmutableMeasureModel medianAge() {
        return ImmutableMeasureModel.builder()
            .label("Age")
            .formId(survey.getFormId())
            .formula(survey.getAgeFieldId().asString())
            .addStatistics(Statistic.MEDIAN)
            .build();
    }

    private ImmutableMeasureModel numChildren() {
        return ImmutableMeasureModel.builder()
                .label("# Children")
                .formId(survey.getFormId())
                .formula(survey.getChildrenFieldId().asString())
                .build();
    }

    private TypeSafeMatcher<Point> point(double value, String... dimensions) {
        return point(value, Statistic.SUM, dimensions);
    }

    private TypeSafeMatcher<Point> point(double value, Statistic statistic, String... dimensions) {
        return new TypeSafeMatcher<Point>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(new Point(value, Integer.toString((int) value), dimensions));
            }

            @Override
            protected boolean matchesSafely(Point point) {

                double absDiff = Math.abs(point.getValue() - value);
                if(absDiff >= 1.0) {
                    return false;
                }

                for (int i = 0; i < dimensions.length; i++) {
                    if (!dimensions[i].equals(point.getCategory(i))) {
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
        if(DUMP_RAW_DATA) {
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
                    if (cell != null) {
                        cells = cell.toString();
                    }
                    System.out.print(column(cells));
                }
                System.out.println();
            }
            System.out.flush();
        }
    }

    private void dump(AnalysisResult result) {

        for (DimensionModel dimensionModel : result.getDimensionSet()) {
            System.out.print(column(dimensionModel.getLabel()));
        }
        System.out.println(column("Value"));

        for (Point point : result.getPoints()) {
            for (int i = 0; i < result.getDimensionSet().getCount(); i++) {
                System.out.print(column(point.getCategory(i)));
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