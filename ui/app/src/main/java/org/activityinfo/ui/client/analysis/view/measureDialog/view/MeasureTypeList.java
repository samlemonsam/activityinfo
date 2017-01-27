package org.activityinfo.ui.client.analysis.view.measureDialog.view;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ListView;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;

/**
 * Allows the user to choose a new measure type from the selected form
 */
public class MeasureTypeList implements IsWidget {


    private static class Item {

        private String key;
        private String label;

        public Item(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private static class ItemKeyProvider implements ModelKeyProvider<Item> {

        @Override
        public String getKey(Item item) {
            return item.getLabel();
        }
    }

    private static class ItemValueProvider implements ValueProvider<Item, String> {

        @Override
        public String getValue(Item object) {
            return object.getLabel();
        }

        @Override
        public void setValue(Item object, String value) {
        }

        @Override
        public String getPath() {
            return "label";
        }
    }

    private Observable<Optional<FormClass>> selectedForm;
    private ListStore<Item> listStore;
    private ListView<Item, String> listView;

    public MeasureTypeList(Observable<Optional<FormClass>> selectedForm) {
        this.selectedForm = selectedForm;
        this.listStore = new ListStore<>(new ItemKeyProvider());
        this.listView = new ListView<>(listStore, new ItemValueProvider());

        selectedForm.subscribe(new Observer<Optional<FormClass>>() {
            @Override
            public void onChange(Observable<Optional<FormClass>> observable) {
                if(observable.isLoading()) {
                    // Loading - clear existing item until complete
                    listStore.clear();
                } else {
                    if(!observable.get().isPresent()) {
                        // No selection: empty list
                        listStore.clear();
                    } else {
                        populateListStore(observable.get().get());
                    }
                }
            }

        });
    }

    @Override
    public Widget asWidget() {
        return listView;
    }


    private void populateListStore(FormClass formClass) {
        listStore.clear();
        listStore.add(new Item("_count", "Count"));
        listStore.add(new Item("_calc", "Calculation"));

        for (FormField field : formClass.getFields()) {
            if(field.getType() instanceof QuantityType) {
                listStore.add(new Item(field.getId().asString(), field.getLabel()));
            } else if(field.getType() instanceof TextType) {
                listStore.add(new Item(field.getId().asString(), field.getLabel()));
            }
        }
    }
}
