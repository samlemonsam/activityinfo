package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.base.Supplier;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

import java.util.List;

/**
 * A single level within the label
 */
public class LevelWidget implements IsWidget, LevelView {

    private final HTMLPanel panel;
    private boolean enabled = true;
    private boolean readOnly;
    private ResourceId parentId;

    private Level level;
    private Supplier<Promise<List<Choice>>> choiceSupplier;
    private List<Choice> choices;
    private Choice selection;

    private SimpleEventBus eventBus = new SimpleEventBus();


    interface LevelWidgetUiBinder extends UiBinder<HTMLPanel, LevelWidget> {
    }

    private static LevelWidgetUiBinder ourUiBinder = GWT.create(LevelWidgetUiBinder.class);

    @UiField
    Element levelLabel;

    @UiField
    ListBox listBox;

    @UiField
    Button clearButton;

    public LevelWidget(String label) {
        panel = ourUiBinder.createAndBindUi(this);
        levelLabel.setInnerText(label);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        listBox.setEnabled(enabled);
    }

    @Override
    public void setSelection(Choice selection) {
        this.selection = selection;
        listBox.clear();
        listBox.addItem(selection.getLabel());
        listBox.addItem(I18N.CONSTANTS.loading());
        listBox.setSelectedIndex(0);
    }

    @Override
    public void setChoices(Supplier<Promise<List<Choice>>> choices) {
        choiceSupplier = choices;
    }

    @Override
    public void clearSelection() {
        this.selection = null;
        this.choices = null;
        listBox.clear();
        listBox.addItem(I18N.CONSTANTS.loading());
        listBox.setSelectedIndex(-1);
    }

    @UiHandler("listBox")
    public void onDropDown(ClickEvent event) {
        if(choices == null) {
            Scheduler.get().scheduleDeferred(new Command() {
                @Override
                public void execute() {
                    populateListBox();
                }
            });
        } else {
            // sometimes this gets confused with a selection
            maybeFireChange();
        }
    }

    @UiHandler("clearButton")
    public void onClear(ClickEvent event) {
        if(selection != null) {
            selection = null;
            clearSelection();
            SelectionEvent.fire(this, null);
        }
    }

    @UiHandler("listBox")
    public void onChange(ChangeEvent event) {
        if(choices != null) {
            maybeFireChange();
        }
    }

    private void maybeFireChange() {
        int selectedIndex = listBox.getSelectedIndex();
        if(selectedIndex != -1) {
            Choice newSelection = choices.get(selectedIndex);
            if(this.selection == null ||
                    !this.selection.getRef().equals(newSelection.getRef())) {

                this.selection = newSelection;
                SelectionEvent.fire(this, newSelection);
            }
        }
    }

    private void populateListBox() {

        choiceSupplier.get().then(new AsyncCallback<List<Choice>>() {

            @Override
            public void onFailure(Throwable caught) {
                listBox.addItem(I18N.CONSTANTS.connectionProblem());
            }

            @Override
            public void onSuccess(List<Choice> result) {
                choices = result;
                Log.info("Received " + result.size());

                listBox.clear();
                for(Choice choice : choices) {
                    String label = choice.getLabel();
                    Log.info("Label = " + label);
                    listBox.addItem(label);
                }
                if(selection != null) {
                    applySelection();
                }
            }
        });
    }

    private void applySelection() {
        for(int i=0;i!=choices.size();++i) {
            if(choices.get(i).getRef().equals(selection.getRef())) {
                listBox.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Choice> handler) {
        return eventBus.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }
}