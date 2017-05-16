package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.*;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.viewModel.ReferenceChoice;
import org.activityinfo.ui.client.input.viewModel.ReferenceChoices;

public class ReferenceFieldWidget implements FieldWidget {

    private ListStore<ReferenceChoice> store;
    private ComboBox<ReferenceChoice> comboBox;

    private ReferenceChoices choices;

    private Subscription choiceSubscription;


    public ReferenceFieldWidget(FieldUpdater fieldUpdater) {


        store = new ListStore<>(ReferenceChoice::getKey);

        ListLoader<ListLoadConfig, ListLoadResult<ReferenceChoice>> loader = new ListLoader<>(new DataProxy<ListLoadConfig, ListLoadResult<ReferenceChoice>>() {
            @Override
            public void load(ListLoadConfig loadConfig, Callback<ListLoadResult<ReferenceChoice>, Throwable> callback) {
                choiceSubscription = choices.getChoices().subscribe(observable -> {

                    // Callback only once
                    if(choiceSubscription == null) {
                        return;
                    }

                    if(observable.isLoaded()) {
                        choiceSubscription.unsubscribe();
                        choiceSubscription = null;
                        callback.onSuccess(new ListLoadResultBean<>(observable.get().buildChoiceList()));
                    }
                });
            }
        });

        comboBox = new ComboBox<>(store, ReferenceChoice::getLabel);
        comboBox.setForceSelection(true);
        comboBox.setUseQueryCache(false);
        comboBox.setLoader(loader);
        comboBox.addValueChangeHandler(new ValueChangeHandler<ReferenceChoice>() {
            @Override
            public void onValueChange(ValueChangeEvent<ReferenceChoice> event) {
                fieldUpdater.update(new FieldInput(new ReferenceValue(event.getValue().getRef())));
            }
        });

        loader.addLoaderHandler(new LoaderHandler<ListLoadConfig, ListLoadResult<ReferenceChoice>>() {
            @Override
            public void onBeforeLoad(BeforeLoadEvent<ListLoadConfig> event) {

            }

            @Override
            public void onLoadException(LoadExceptionEvent<ListLoadConfig> event) {

            }

            @Override
            public void onLoad(LoadEvent<ListLoadConfig, ListLoadResult<ReferenceChoice>> event) {
                store.replaceAll(event.getLoadResult().getData());
            }
        });

    }

    public void setChoices(ReferenceChoices choices) {
        this.choices = choices;
    }


    @Override
    public void setRelevant(boolean relevant) {
        comboBox.setEnabled(relevant);
    }

    @Override
    public Widget asWidget() {
        return comboBox;
    }
}
