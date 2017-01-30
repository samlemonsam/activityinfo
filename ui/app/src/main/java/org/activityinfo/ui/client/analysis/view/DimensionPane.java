package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.DimensionSet;
import org.activityinfo.ui.client.analysis.model.DimensionSourceModel;

/**
 *
 */
public class DimensionPane implements IsWidget {

    private AnalysisModel model;
    private NewDimensionDialog dialog;
    private ContentPanel contentPanel;

    private ListStore<DimensionModel> listStore;
    private ListView<DimensionModel, String> listView;

    public DimensionPane(AnalysisModel model) {
        this.model = model;

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(this::addDimensionClicked);

        listStore = new ListStore<>(DimensionModel::getId);
        listView = new ListView<>(listStore, new ValueProvider<DimensionModel, String>() {
            @Override
            public String getValue(DimensionModel object) {
                return object.getLabel();
            }

            @Override
            public void setValue(DimensionModel object, String value) {
            }

            @Override
            public String getPath() {
                return "label";
            }
        });

        contentPanel = new ContentPanel();
        contentPanel.setHeading("Row Dimensions");
        contentPanel.addTool(addButton);
        contentPanel.setWidget(listView);

        model.getDimensions().subscribe(new Observer<DimensionSet>() {
            @Override
            public void onChange(Observable<DimensionSet> observable) {
                if (observable.isLoaded()) {
                    listStore.replaceAll(observable.get().getList());
                } else {
                    listStore.clear();
                }
            }
        });
    }

    @Override
    public Widget asWidget() {
        return contentPanel;
    }

    private void addDimensionClicked(SelectEvent event) {
        if (dialog == null) {
            dialog = new NewDimensionDialog(model);
            dialog.addSelectionHandler(this::onNewDimensionSelected);
        }
        dialog.show();
    }

    private void onNewDimensionSelected(SelectionEvent<DimensionSourceModel> event) {
        model.addDimension(event.getSelectedItem());
    }

}
