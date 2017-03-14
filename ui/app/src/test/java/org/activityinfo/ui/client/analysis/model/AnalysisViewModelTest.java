package org.activityinfo.ui.client.analysis.model;

import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTesting;
import org.activityinfo.observable.Observer;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;


@Ignore("WIP")
public class AnalysisViewModelTest {

    @Before
    public void setupI18N() {
        LocaleProxy.initialize();
    }

    @Test
    public void testEmptyModel() {
        TestingFormStore formStore = new TestingFormStore();
        AnalysisViewModel model = new AnalysisViewModel(formStore);

        AnalysisResult result = assertLoads(model.getResultTable());
    }


    @Test
    public void testSimpleCount() {

        TestingFormStore formStore = new TestingFormStore();
        formStore.delayLoading();


        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(
            new MeasureModel(ResourceId.generateCuid(),
                "Count",
                Survey.FORM_ID, "1"));

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
    public void dimensions() {

        AnalysisModel model = new AnalysisModel();
        model.getMeasures().add(
                new MeasureModel(ResourceId.generateCuid(),
                        "Count",
                        Survey.FORM_ID, "1"));

        model.getDimensions().add(
                new DimensionModel(ResourceId.generateCuid(),
                        "Gender",
                        new DimensionMapping(new SymbolExpr("Gender"))));


        TestingFormStore formStore = new TestingFormStore();
        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        viewModel.updateModel(model);

        AnalysisResult result = assertLoads(viewModel.getResultTable());

        assertThat(result.getPoints(), hasSize(1));
        assertThat(result.getPoints().get(0).getValue(), equalTo(0d));
    }

    private <T> T assertLoads(Observable<T> result) {
        List<T> results = new ArrayList<>();
        result.subscribe(new Observer<T>() {
            @Override
            public void onChange(Observable<T> observable) {
                if (observable.isLoaded()) {
                    results.add(observable.get());
                }
            }
        });
        if (results.isEmpty()) {
            throw new AssertionError("Observable did not load synchronously.");
        }
        return Iterables.getLast(results);
    }
}