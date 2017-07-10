package org.activityinfo.ui.client.analysis;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.ui.client.analysis.view.AnalysisView;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.store.FormStore;


public class AnalysisActivity extends AbstractActivity {
    private FormStore formStore;
    private AnalysisPlace place;

    public AnalysisActivity(FormStore formStore, AnalysisPlace place) {
        this.formStore = formStore;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {

        AnalysisViewModel model = new AnalysisViewModel(formStore, place.getId());

        AnalysisView view = new AnalysisView(model);
        panel.setWidget(view);
    }
}
