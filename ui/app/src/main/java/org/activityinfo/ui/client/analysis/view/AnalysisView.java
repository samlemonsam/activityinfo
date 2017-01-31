package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;

public class AnalysisView implements IsWidget {

    private BorderLayoutContainer container;
    private AnalysisModel model;

    public AnalysisView(AnalysisModel model) {
        this.model = model;
        this.model = model;
        container = new BorderLayoutContainer();
        createPanes();
    }

    private void createPanes() {

        MeasurePane measurePane = new MeasurePane(model);
        DimensionPane rowPane = new DimensionPane(model);
        DimensionPane columnPane = new DimensionPane(model);
        PivotTableView pivotTableView = new PivotTableView(model);

        VerticalLayoutContainer pane = new VerticalLayoutContainer();
        pane.add(measurePane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.4));
        pane.add(rowPane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.3));
        pane.add(columnPane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.3));

        BorderLayoutContainer.BorderLayoutData paneLayout = new BorderLayoutContainer.BorderLayoutData();
        paneLayout.setSize(0.3); // 30% of view
        paneLayout.setMaxSize(400);
        container.setEastWidget(pane, paneLayout);

        container.setCenterWidget(pivotTableView);
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
