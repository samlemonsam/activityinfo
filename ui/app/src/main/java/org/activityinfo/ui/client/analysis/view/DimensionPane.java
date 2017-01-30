package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;

/**
 *
 */
public class DimensionPane implements IsWidget {

    private AnalysisModel model;

    private ContentPanel contentPanel;

    public DimensionPane(AnalysisModel model) {
        this.model = model;
        this.contentPanel = new ContentPanel();
        this.contentPanel.setHeading("Row Dimensions");
    }


    @Override
    public Widget asWidget() {
        return contentPanel;
    }
}
