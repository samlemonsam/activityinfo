package org.activityinfo.ui.client.analysis.view;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModelTest;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MeasureListItemStoreTest {

    private TestingFormStore formStore;

    @Before
    public void setup() {
        LocaleProxy.initialize();

        formStore = new TestingFormStore();
    }

    @Test
    public void test() {

        formStore.delayLoading();

        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        MeasureListItemStore store = new MeasureListItemStore(viewModel);

        viewModel.addMeasure(AnalysisViewModelTest.surveyCount());

        assertThat(store.size(), equalTo(0));

        formStore.loadAll();


        assertThat(store.size(), equalTo(1));
    }

}