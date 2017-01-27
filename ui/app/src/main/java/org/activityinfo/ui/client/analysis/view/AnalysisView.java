package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.ui.client.store.FormStore;

public class AnalysisView implements IsWidget {

    private BorderLayoutContainer container;
    private FormStore formStore;

    public AnalysisView(FormStore formStore) {
        this.formStore = formStore;
        container = new BorderLayoutContainer();
        createPanes();
    }

    private void createPanes() {

        MeasurePane measurePane = new MeasurePane(formStore);

        VerticalLayoutContainer pane = new VerticalLayoutContainer();
        pane.add(measurePane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.3));
        container.setEastWidget(pane);
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
