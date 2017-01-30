package org.activityinfo.ui.client.analysis.model;

import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class AnalysisModelTest {

    @Before
    public void setupI18N() {
        LocaleProxy.initialize();
    }

    @Test
    public void test() {

        TestingFormStore formStore = new TestingFormStore();
        AnalysisModel model = new AnalysisModel(formStore);
        model.addMeasure(new CountMeasure(TestingFormStore.SURVEY_FORM_ID));

        AnalysisResult result = assertLoads(model.getResult());


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