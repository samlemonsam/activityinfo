package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;

public class AnalysisView implements IsWidget {

    private BorderLayoutContainer container;
    private AnalysisViewModel model;

    public AnalysisView(AnalysisViewModel model) {
        this.model = model;
        this.model = model;
        container = new BorderLayoutContainer();
        createPanes();

        AnalysisBundle.INSTANCE.getStyles().ensureInjected();
    }

    private void createPanes() {

        MeasurePane measurePane = new MeasurePane(model);
        DimensionPane rowPane = new DimensionPane(model);
      //  DimensionPane columnPane = new DimensionPane(model);
        PivotTableView pivotTableView = new PivotTableView(model);

        VerticalLayoutContainer pane = new VerticalLayoutContainer();
        pane.add(measurePane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.5));
        pane.add(rowPane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.5));
     //   pane.add(columnPane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.3));

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
