package org.activityinfo.ui.client.lookup.view;

import com.google.common.base.Optional;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.event.BeforeQueryEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.observable.SubscriptionSet;
import org.activityinfo.ui.client.lookup.viewModel.LookupKeyViewModel;
import org.activityinfo.ui.client.lookup.viewModel.LookupViewModel;

import java.util.List;
import java.util.Objects;

public class LevelWidget implements IsWidget {

    private LookupViewModel selectViewModel;
    private LookupKeyViewModel level;

    private ListStore<String> store;
    private ComboBox<String> comboBox;
    private FieldLabel fieldLabel;

    private Subscription choiceSubscription;
    private SubscriptionSet subscriptionSet = new SubscriptionSet();

    private boolean relevant = true;

    public LevelWidget(LookupViewModel selectViewModel, LookupKeyViewModel level) {
        this.selectViewModel = selectViewModel;
        this.level = level;

        store = new ListStore<>(key -> key);

        comboBox = new ComboBox<>(new ChoiceCell(level.getChoices(), store));
        comboBox.setWidth(300);

        comboBox.addAttachHandler(this::onAttach);
        comboBox.addBeforeQueryHandler(this::onBeforeQuery);

        comboBox.addSelectionHandler(new SelectionHandler<String>() {
            @Override
            public void onSelection(SelectionEvent<String> event) {
                selectViewModel.select(level.getLookupKey(), event.getSelectedItem());
            }
        });

        this.fieldLabel = new FieldLabel(comboBox, level.getKeyLabel());
    }

    public void addSelectionHandler(SelectionHandler<String> handler) {
        comboBox.addSelectionHandler(handler);
    }

    private void onAttach(AttachEvent attachEvent) {
        if(attachEvent.isAttached()) {
            subscriptionSet.add(level.getSelectedKey().debounce(50).subscribe(this::updateSelectionView));
            subscriptionSet.add(level.isEnabled().subscribe(enabled -> updateEnabledView()));
        } else {
            subscriptionSet.unsubscribeAll();
        }
    }

    private void onBeforeQuery(BeforeQueryEvent<String> event) {
        // Wait until the first time the list is accessed to start listening
        // to the choice list
        if(choiceSubscription == null) {
            choiceSubscription = level.getChoices().subscribe(this::onChoicesChanged);
            subscriptionSet.add(choiceSubscription);
        }
    }

    private void onChoicesChanged(Observable<List<String>> choices) {
        if(choices.isLoading()) {
            store.clear();
        } else {
            store.replaceAll(choices.get());
        }
    }

    private void updateSelectionView(Observable<Optional<String>> selection) {
        if(selection.isLoading()) {
            comboBox.setText(I18N.CONSTANTS.loading());
            comboBox.disable();
        } else {
            String newText = selection.get().orNull();
            if(!Objects.equals(comboBox.getText(), newText)) {
                comboBox.setText(newText);
            }
            updateEnabledView();
        }
    }

    public ComboBox<String> getComboBox() {
        return comboBox;
    }

    @Override
    public Widget asWidget() {
        return fieldLabel;
    }

    public void setRelevant(boolean relevant) {
        this.relevant = relevant;
        updateEnabledView();
    }

    private void updateEnabledView() {
        this.comboBox.setEnabled(relevant &&
            level.getSelectedKey().isLoaded() &&
            level.isEnabled().isLoaded() && level.isEnabled().get());
    }
}
