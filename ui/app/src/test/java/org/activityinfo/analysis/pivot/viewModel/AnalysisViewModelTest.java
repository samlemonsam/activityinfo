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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.pivot.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.util.Pair;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTesting;
import org.activityinfo.store.testing.IntakeForm;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.store.testing.Survey2;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.activityinfo.analysis.pivot.viewModel.Point.TOTAL;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AnalysisViewModelTest {

    private static boolean DUMP_RAW_DATA = true;

    private static final int COLUMN_LENGTH = 20;
    private static final String NA = null;
    private TestingFormStore formStore;
    private Survey survey;
    private IntakeForm intakeForm;
    private Survey2 survey2;

    @Before
    public void setup() {
        LocaleProxy.initialize();

        formStore = new TestingFormStore();
        survey = formStore.getCatalog().getSurvey();
        intakeForm = formStore.getCatalog().getIntakeForm();
        survey2 = formStore.getCatalog().getSurvey2();
    }

    @Test
    public void testEmptyModel() {
        TestingFormStore formStore = new TestingFormStore();
        AnalysisViewModel model = new AnalysisViewModel(formStore);

        AnalysisResult result = assertLoads(model.getResultTable());

        PivotTable pivotTable = assertLoads(model.getPivotTable());
        assertTrue(pivotTable.isEmpty());
    }


    @Test
    public void testSimpleCount() {

        formStore.delayLoading();

        PivotModel model = ImmutablePivotModel.builder()
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

        PivotTable pivotTable = assertLoads(viewModel.getPivotTable());
        assertFalse(pivotTable.isEmpty());

    }


    @Test
    public void testSimpleCSV() {

        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(surveyCount())
            .build();

        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(model);

        Connection<PivotTable> result = ObservableTesting.connect(viewModel.getPivotTable());

        String csv = PivotTableRenderer.renderDelimited(result.assertLoaded(), ",");

        System.out.println(csv);

    }

    @Test
    public void dimensionsWithMissing() {


        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(surveyCount())
            .addDimensions(genderDimension())
            .build();

        assertThat(points(model), containsInAnyOrder(
                point(199, "Male"),
                point(212, "Female")));
    }


    @Test
    public void dimensionsWithSeveralStatistics() {
        PivotModel model = ImmutablePivotModel.builder()
                .addMeasures(medianAge().withStatistics(Statistic.MIN, Statistic.MAX, Statistic.MEDIAN))
                .addDimensions(genderDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(15,   "Male",     "Min"),
                point(15,   "Female",   "Min"),
                point(98,   "Male",     "Max"),
                point(98,   "Female",   "Max"),
                point(56.5, "Male",     "Median"),
                point(51.0, "Female",   "Median")));
    }

    @Test
    public void longForm() {
        PivotModel longFormModel = ImmutablePivotModel.builder()
                .measures(extractAllMeasures(Lists.newArrayList(survey.getFormClass(), survey2.getFormClass())))
                .addDimensions(idDimension(Lists.newArrayList(survey.getFormClass(), survey2.getFormClass())))
                .addAllDimensions(extractAllDimensions(Lists.newArrayList(survey.getFormClass(), survey2.getFormClass())))
                .build();

        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(longFormModel);
        AnalysisResult result = assertLoads(viewModel.getResultTable());
        dump(result);
    }

    private ImmutableDimensionModel idDimension(List<FormClass> formScope) {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Record Id")
                .mappings(extractIdMappings(formScope))
                .axis(Axis.ROW)
                .build();
    }

    private Iterable<? extends DimensionMapping> extractIdMappings(List<FormClass> formScope) {
        return formScope.stream()
                .map(form -> new DimensionMapping(form.getId(), "_id"))
                .collect(Collectors.toList());
    }

    private List<ImmutableMeasureModel> extractAllMeasures(List<FormClass> formScope) {
        return formScope.stream()
                .flatMap(this::extractFormMeasures)
                .collect(Collectors.toList());
    }

    private Stream<ImmutableMeasureModel> extractFormMeasures(FormClass form) {
        return form.getFields().stream()
                .filter(field -> field.getType() instanceof QuantityType)
                .map(measureField -> ImmutableMeasureModel.builder()
                        .formId(form.getId())
                        .label(measureField.getLabel())
                        .formula(measureField.getId().asString())
                        .build());
    }

    private List<ImmutableDimensionModel> extractAllDimensions(List<FormClass> formScope) {
        Multimap<String,DimensionMapping> dimensionGroups = formScope.stream()
                .flatMap(this::extractFormDimensions)
                .collect(Multimaps.toMultimap(
                        dimLabelMappingPair -> dimLabelMappingPair.getFirst(),
                        dimLabelMappingPair -> dimLabelMappingPair.getSecond(),
                        ArrayListMultimap::create));

        return dimensionGroups.asMap().entrySet().stream()
                .map(dimensionGroup -> ImmutableDimensionModel.builder()
                        .id(ResourceId.generateCuid())
                        .label(dimensionGroup.getKey())
                        .mappings(dimensionGroup.getValue())
                        .axis(Axis.COLUMN)
                        .build())
                .collect(Collectors.toList());
    }

    private Stream<Pair<String,DimensionMapping>> extractFormDimensions(FormClass form) {
        return form.getFields().stream()
                .filter(field -> {
                    if (field.getType() instanceof QuantityType) {
                        return false;
                    }
                    if (field.getType() instanceof EnumType) {
                        EnumType enumType = (EnumType) field.getType();
                        return enumType.getCardinality() == Cardinality.SINGLE;
                    }
                    if (field.getType() instanceof AttachmentType) {
                        return false;
                    }
                    return true;
                })
                .map(dimensionField -> Pair.newPair(
                        dimensionField.getLabel().toLowerCase().trim(),
                        new DimensionMapping(form.getId(), dimensionField.getId())));
    }

    @Test
    public void pivotDimensionsWithSeveralStatistics() {
        PivotModel model = ImmutablePivotModel.builder()
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
        PivotModel model = ImmutablePivotModel.builder()
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
        PivotModel model = ImmutablePivotModel.builder()
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

        PivotModel model = ImmutablePivotModel.builder()
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

        PivotModel model = ImmutablePivotModel.builder()
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

        PivotModel model = ImmutablePivotModel.builder()
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
                ImmutablePivotModel.builder()
                .addMeasures(surveyCount())
                .build());

        ImmutableDimensionModel genderDimension = genderDimension();

        // Add a new dimension
        viewModel.updateModel(viewModel.getWorkingModel().withDimension(genderDimension));
        assertThat(assertLoads(viewModel.getDimensionListItems()), hasSize(1));

        // Delete a non-existant dimension
        viewModel.updateModel(viewModel.getWorkingModel().withoutDimension("FOOOO"));
        assertThat(assertLoads(viewModel.getDimensionListItems()), hasSize(1));

        // Delete the gender dimension
        viewModel.updateModel(viewModel.getWorkingModel().withoutDimension(genderDimension.getId()));
        assertThat(assertLoads(viewModel.getDimensionListItems()), hasSize(0));
    }


    @Test
    public void twoDimensions() {

        dumpQuery(survey.getFormId(), "Gender", "Married", "Age");

        PivotModel model = ImmutablePivotModel.builder()
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

        PivotModel model = ImmutablePivotModel.builder()
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

        PivotModel model = ImmutablePivotModel.builder()
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


        PivotModel model = ImmutablePivotModel.builder()
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
        PivotModel model = ImmutablePivotModel.builder()
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

    private String pivot(PivotModel model) {
        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(serializeAndDeserialize(model));

        AnalysisResult analysisResult = assertLoads(viewModel.getResultTable());
        PivotTable table = new PivotTable(analysisResult);

        String text = PivotTableRenderer.renderPlainText(table);

        System.out.println(text);

        return text;
    }

    private PivotModel serializeAndDeserialize(PivotModel model) {
        JsonValue json = model.toJson();
        PivotModel deserialized = PivotModel.fromJson(json);
        assertThat(deserialized, equalTo(model));
        return deserialized;
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

        PivotModel model = ImmutablePivotModel.builder()
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
        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(intakeCaseCount().withLabel("Cases"))
            .addMeasures(numChildren().withLabel("Children"))
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(1127,   "Cases"),
            point(1525,   "Children")));
    }

    @Test
    public void multipleMeasuresAndDimensions() {
        PivotModel model = ImmutablePivotModel.builder()
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
        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(intakeCaseCount().withLabel("Cases"))
            .addMeasures(numChildren().withLabel("Children"))
            .addDimensions(this.caseYear().withMissingIncluded(true))
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(557,       "2016",   "Cases"),
            point(570,       "2017",   "Cases"),
            point(1525,       "None",   "Children")));
    }


    @Test
    public void dateDimension() {
        PivotModel model = ImmutablePivotModel.builder()
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

        dumpQuery(intakeForm.getFormId(), intakeForm.getNationalityFieldId().asString());

        PivotModel model = ImmutablePivotModel.builder()
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
    public void multiDimensionsAndSingleValueDimsWithPercentages() {

        dumpQuery(intakeForm.getFormId(), intakeForm.getNationalityFieldId().asString());

        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(intakeCaseCount())
            .addDimensions(caseYear())
            .addDimensions(nationality().withPercentage(true).withTotals(true))
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(411,   "2016",   "Palestinian", "Sum"),
            point(138,   "2016",   "Jordanian",   "Sum"),
            point(71,    "2016",   "Syrian",      "Sum"),
            point(557,   "2016",   TOTAL),

            point("74%",  "2016",   "Palestinian", "%"),
            point("25%",  "2016",   "Jordanian",   "%"),
            point("13%",  "2016",   "Syrian",      "%"),
            point("100%", "2016",   TOTAL),

            point(422,   "2017",   "Palestinian", "Sum"),
            point(144,   "2017",   "Jordanian",   "Sum"),
            point(84,    "2017",   "Syrian",      "Sum"),
            point(570,   "2017",   TOTAL),

            point("74%",  "2017",   "Palestinian", "%"),
            point("25%",  "2017",   "Jordanian",   "%"),
            point("15%",  "2017",   "Syrian",      "%"),
            point("100%", "2017",   TOTAL)

            ));
    }


    @Test
    public void multiDimensionsWithTotals() {

        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(intakeCaseCount())
            .addDimensions(nationality().withMissingIncluded(true).withTotals(true))
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(833,   "Palestinian"),
            point(282,   "Jordanian"),
            point(155,   "Syrian"),
            point(219,   "None"),
            point(1127,  TOTAL)));
    }

    @Test
    public void multiDimensionsWithTotalsPercentages() {

        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(intakeCaseCount())
            .addDimensions(nationality()
                    .withMissingIncluded(true)
                    .withTotals(true)
                    .withPercentage(true))
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(833,   "Palestinian", "Sum"),
            point(282,   "Jordanian", "Sum"),
            point(155,   "Syrian", "Sum"),
            point(219,   "None", "Sum"),
            point(1127,  TOTAL, "Sum"),
            point( "74%",  "Palestinian", "%"),
            point( "25%",  "Jordanian", "%"),
            point( "14%",  "Syrian", "%"),
            point( "19%",  "None", "%"),
            point("100%",  TOTAL, "%")
        ));
    }


    @Test
    public void multiDimensionsWithMissing() {

        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(intakeCaseCount())
            .addDimensions(nationality().withMissingIncluded(true))
            .build();

        assertThat(points(model), containsInAnyOrder(
            point(833,   "Palestinian"),
            point(282,   "Jordanian"),
            point(155,   "Syrian"),
            point(219,   "None")));
    }

    @Test
    public void countDistinct() {

        PivotModel model = ImmutablePivotModel.builder()
            .addMeasures(distinctRegNumbers())
            .build();

        assertThat(points(model), contains(point(496)));
    }

    @Test
    public void severalMultiDimensions() {

        PivotModel model = ImmutablePivotModel.builder()
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

    @Test
    public void severalMultiDimensionsWithTotals() {

        PivotModel model = ImmutablePivotModel.builder()
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

        dumpQuery(survey.getFormId(), survey.getAgeFieldId().asString(), survey.getGenderFieldId().asString(), survey.getMarriedFieldId().asString());

        PivotModel model = ImmutablePivotModel.builder()
                .addMeasures(medianAge())
                .addDimensions(genderDimension().withTotals(true))
                .addDimensions(marriedDimension().withTotals(true))
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(52.5,  "Male",   "Married"),
                point(61.5,  "Male",   "Single"),
                point(56.0,  "Female", "Married"),
                point(52.0, "Female", "Single"),
                point(54.0, TOTAL,  "Married"),
                point(56.0, TOTAL,  "Single"),
                point(55.0, "Male",   TOTAL),
                point(55.0, "Female", TOTAL),
                point(55.0,  TOTAL,  TOTAL)));
    }

    @Test
    public void median() {

        //dumpQuery(Survey.FORM_ID, "Gender", "age");

        PivotModel model = ImmutablePivotModel.builder()
                .addMeasures(medianAge())
                .addDimensions(genderDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(56.5, "Male"),
                point(51.0, "Female")));
    }

    @Test
    public void medianWithMissing() {

        PivotModel model = ImmutablePivotModel.builder()
                .addMeasures(numChildren().withStatistics(Statistic.MEDIAN))
                .addDimensions(genderDimension())
                .build();

        assertThat(points(model), containsInAnyOrder(
                point(3.0, "Male"),
                point(4.0, "Female")));
    }

    private ImmutableDimensionModel genderDimension() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Gender")
                .missingIncluded(false)
                .addMappings(new DimensionMapping(new CompoundExpr(survey.getFormId(), "Gender")))
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
                .addMappings(new DimensionMapping(new SymbolNode("MARRIED")))
                .missingIncluded(false)
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

    private ImmutableMeasureModel distinctRegNumbers() {
        return ImmutableMeasureModel.builder()
            .label("Registered Individuals")
            .addStatistics(Statistic.COUNT_DISTINCT)
            .formId(intakeForm.getFormId())
            .formula(intakeForm.getRegNumberFieldId().asString())
            .build();

    }

    private ImmutableDimensionModel caseYear() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Year")
                .missingIncluded(false)
                .addMappings(new DimensionMapping(intakeForm.getFormId(), intakeForm.getOpenDateFieldId()))
                .dateLevel(DateLevel.YEAR)
                .build();
    }


    private ImmutableDimensionModel caseQuarter() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Quarter")
                .missingIncluded(false)
                .addMappings(new DimensionMapping(intakeForm.getFormId(), intakeForm.getOpenDateFieldId()))
                .dateLevel(DateLevel.QUARTER)
                .build();
    }


    private ImmutableDimensionModel nationality() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Nationality")
                .missingIncluded(false)
                .addMappings(new DimensionMapping(intakeForm.getFormId(), intakeForm.getNationalityFieldId()))
                .build();
    }

    private ImmutableDimensionModel protectionProblem() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Problem")
                .missingIncluded(false)
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


    private TypeSafeMatcher<Point> point(String formattedValue, String... dimensions) {
        return new TypeSafeMatcher<Point>() {
            @Override
            protected boolean matchesSafely(Point item) {
                if(!item.getFormattedValue().equals(formattedValue)) {
                    return false;
                }
                for (int i = 0; i < dimensions.length; i++) {
                    if (!dimensions[i].equals(item.getCategory(i))) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Point(%s, %s)", formattedValue, Joiner.on(",").join(dimensions)));
            }
        };
    }


    private TypeSafeMatcher<Point> point(double value, String... dimensions) {
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
    private List<Point> points(PivotModel model) {
        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(model);

        AnalysisResult result = assertLoads(viewModel.getResultTable());

        dump(result);

        return result.getPoints();
    }

    private void dumpQuery(ResourceId formId, String... columns) {
        if(DUMP_RAW_DATA) {
            try {
                File tempFile = File.createTempFile("query", ".csv");
                try (PrintWriter writer = new PrintWriter(tempFile)) {
                    QueryModel model = new QueryModel(formId);
                    for (int i = 0; i < columns.length; i++) {
                        model.selectExpr(columns[i]).as("c" + i);
                    }
                    ColumnSet columnSet = assertLoads(formStore.query(model));

                    for (int i = 0; i < columns.length; i++) {
                        if(i > 0) {
                            writer.print(",");
                        }
                        writer.print(columns[i]);
                    }
                    writer.println();

                    for (int i = 0; i < columnSet.getNumRows(); i++) {
                        for (int j = 0; j < columns.length; j++) {
                            if(j > 0) {
                                writer.print(",");
                            }
                            ColumnView columnView = columnSet.getColumnView("c" + j);
                            Object cell = columnView.get(i);
                            String cells = "";
                            if (cell != null) {
                                cells = cell.toString();
                            }
                            writer.print(cells);
                        }
                        writer.println();
                    }
                }
                System.out.println("Dumped data to " + tempFile);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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