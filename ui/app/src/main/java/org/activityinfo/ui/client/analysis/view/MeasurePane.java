package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.analysis.view.measureDialog.view.MeasureDialog;


public class MeasurePane implements IsWidget {

    private ContentPanel contentPanel;
    private MeasureDialog dialog;

    private ObservingListStore<MeasureModel> store;
    private ListView<MeasureModel, String> list;
    private AnalysisModel model;


    public MeasurePane(final AnalysisModel model) {
        this.model = model;

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(this::addMeasureClicked);

        store = new ObservingListStore<>(model.getMeasures(), MeasureModel::getKey);
        list = new ListView<>(store, new ValueProvider<MeasureModel, String>() {
            @Override
            public String getValue(MeasureModel object) {
                return object.getLabel();
            }

            @Override
            public void setValue(MeasureModel object, String value) {

            }

            @Override
            public String getPath() {
                return "label";
            }
        });
        this.contentPanel = new ContentPanel();
        this.contentPanel.setHeading("Measures");
        this.contentPanel.addTool(addButton);
        this.contentPanel.setWidget(list);
    }


    private void addMeasureClicked(SelectEvent event) {
        if (dialog == null) {
            dialog = new MeasureDialog(model.getFormStore());
            dialog.addSelectionHandler(this::measureAdded);
        }
        dialog.show();
    }

    private void measureAdded(SelectionEvent<MeasureModel> measure) {
        model.addMeasure(measure.getSelectedItem());
    }

    @Override
    public Widget asWidget() {
        return contentPanel;
    }
}
