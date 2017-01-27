package org.activityinfo.ui.client.analysis;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.ui.client.analysis.view.AnalysisView;
import org.activityinfo.ui.client.store.FormStore;


public class AnalysisActivity extends AbstractActivity {
    private FormStore formStore;

    public AnalysisActivity(FormStore formStore) {
        this.formStore = formStore;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        AnalysisView view = new AnalysisView(formStore);
        panel.setWidget(view);
    }
}
