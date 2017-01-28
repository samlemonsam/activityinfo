package org.activityinfo.ui.client.analysis.view.measureDialog.view;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ListView;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.analysis.view.measureDialog.model.MeasureSelectionModel;
import org.activityinfo.ui.client.analysis.view.measureDialog.model.MeasureType;

import java.util.List;

/**
 * Allows the user to choose from available measure types.
 */
public class MeasureTypeListView implements IsWidget {


    private MeasureSelectionModel model;
    private SimpleEventBus eventBus = new SimpleEventBus();

    private static class TypeKeyProvider implements ModelKeyProvider<MeasureType> {

        @Override
        public String getKey(MeasureType item) {
            return item.getLabel();
        }
    }

    private static class TypeValueProvider implements ValueProvider<MeasureType, String> {

        @Override
        public String getValue(MeasureType object) {
            return object.getLabel();
        }

        @Override
        public void setValue(MeasureType object, String value) {
        }

        @Override
        public String getPath() {
            return "label";
        }
    }

    private ListStore<MeasureType> listStore;
    private ListView<MeasureType, String> listView;

    public MeasureTypeListView(final MeasureSelectionModel model) {
        this.model = model;
        this.listStore = new ListStore<>(new TypeKeyProvider());
        this.listView = new ListView<>(listStore, new TypeValueProvider());

        this.model.getAvailableMeasures().subscribe(new Observer<List<MeasureType>>() {
            @Override
            public void onChange(Observable<List<MeasureType>> measures) {
                if (measures.isLoading()) {
                    listStore.clear();
                } else {
                    listStore.replaceAll(measures.get());
                    if (!model.getSelectedMeasureType().isLoading() && model.getSelectedMeasureType().get().isPresent()) {
                        MeasureType selectedMeasure = model.getSelectedMeasureType().get().get();
                        listView.getSelectionModel().select(selectedMeasure, false);
                    }
                }
            }
        });
    }

    @Override
    public Widget asWidget() {
        return listView;
    }


    public HandlerRegistration addSelectionChangedHandler(SelectionHandler<MeasureType> handler) {
        return listView.getSelectionModel().addSelectionHandler(handler);
    }

}
