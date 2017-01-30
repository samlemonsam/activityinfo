package org.activityinfo.ui.client.analysis.model;

import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableTesting;
import org.activityinfo.observable.Observer;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class AnalysisModelTest {

    @Before
    public void setupI18N() {
        LocaleProxy.initialize();
    }

    @Test
    public void testEmptyModel() {
        TestingFormStore formStore = new TestingFormStore();
        AnalysisModel model = new AnalysisModel(formStore);

        assertTrue(model.getResult().isLoading());

        AnalysisResult result = assertLoads(model.getResult());


    }


    @Test
    public void testSimpleCount() {

        TestingFormStore formStore = new TestingFormStore();
        formStore.delayLoading();

        AnalysisModel model = new AnalysisModel(formStore);
        model.addMeasure(new CountMeasure(Survey.FORM_ID));

        Connection<AnalysisResult> result = ObservableTesting.connect(model.getResult());
        result.assertLoading();

        formStore.loadAll();

        List<Point> points = result.assertLoaded().getPoints();

        assertThat(points, hasSize(1));
        assertThat(points.get(0).getValue(), equalTo(0d));
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