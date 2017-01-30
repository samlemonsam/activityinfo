package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.DimensionSourceModel;
import org.activityinfo.ui.client.analysis.model.DimensionSourceSet;

/**
 * Allows the user to choose a new dimension
 */
public class NewDimensionDialog implements HasSelectionHandlers<DimensionSourceModel> {

    private AnalysisModel model;
    private Dialog dialog;
    private Subscription subscription;

    private final SimpleEventBus eventBus = new SimpleEventBus();

    private ListStore<DimensionSourceModel> listStore;
    private ListView<DimensionSourceModel, String> listView;


    public NewDimensionDialog(AnalysisModel model) {
        this.model = model;

        listStore = new ListStore<>(item -> item.getLabel());
        listView = new ListView<>(listStore, new ValueProvider<DimensionSourceModel, String>() {
            @Override
            public String getValue(DimensionSourceModel object) {
                return object.getLabel();
            }

            @Override
            public void setValue(DimensionSourceModel object, String value) {
            }

            @Override
            public String getPath() {
                return "label";
            }
        });

        this.dialog = new Dialog();
        dialog.setHeading("New Dimension");
        dialog.setPixelSize(640, 480);
        dialog.setResizable(true);
        dialog.setPredefinedButtons(Dialog.PredefinedButton.CANCEL, Dialog.PredefinedButton.OK);
        dialog.setClosable(true);
        dialog.addDialogHideHandler(this::onDialogHidden);
        dialog.setWidget(listView);

        dialog.getButton(Dialog.PredefinedButton.CANCEL).addSelectHandler(this::onCancelClicked);
        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(this::onOkClicked);
    }


    public void show() {
        subscription = model.getDimensionSources().subscribe(this::onTreeUpdated);
        dialog.show();
        dialog.center();
    }

    private void onTreeUpdated(Observable<DimensionSourceSet> dimensionSources) {
        if (dimensionSources.isLoading()) {
            listStore.clear();
        } else {
            listStore.replaceAll(dimensionSources.get().getSources());
        }
    }

    private void onOkClicked(SelectEvent event) {
        DimensionSourceModel selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            SelectionEvent.fire(this, selectedItem);
            dialog.hide();
        }
    }

    private void onCancelClicked(SelectEvent event) {
        dialog.hide();
    }


    private void onDialogHidden(DialogHideEvent dialogHideEvent) {
        subscription.unsubscribe();
    }


    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<DimensionSourceModel> selectionHandler) {
        return eventBus.addHandler(SelectionEvent.getType(), selectionHandler);
    }

    @Override
    public void fireEvent(GwtEvent<?> gwtEvent) {
        eventBus.fireEvent(gwtEvent);
    }
}
