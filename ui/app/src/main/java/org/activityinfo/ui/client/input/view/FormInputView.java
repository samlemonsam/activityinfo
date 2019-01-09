/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.CloseEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.json.Json;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.promise.Maybe;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModelBuilder;
import org.activityinfo.ui.client.input.viewModel.FormStructure;
import org.activityinfo.ui.client.input.viewModel.LiveActivePeriodMemory;
import org.activityinfo.ui.client.store.FormStore;

import java.util.logging.Logger;

/**
 * Root view for a {@link FormInputModel}
 */
public class FormInputView implements IsWidget, InputHandler {

    private static final Logger LOGGER = Logger.getLogger(FormInputView.class.getName());

    private final Observable<FormStructure> formStructure;

    /**
     * True if we've finished the initial load of the form structure and
     * existing record. If either changes after the initial load, we alter the
     * user but do not immediately screw with the form.
     */
    private boolean initialLoad = false;

    private FormPanel formPanel = null;

    private FormStore formStore;
    private Maybe<RecordTree> existingRecord;
    private FormInputModel inputModel;
    private FormInputViewModelBuilder viewModelBuilder = null;

    private FormInputViewModel viewModel = null;

    private VerticalLayoutContainer container;

    private Subscription structureSubscription;


    public FormInputView(FormStore formStore, RecordRef recordRef) {
        this.formStore = formStore;
        this.formStructure = FormStructure.fetch(formStore, recordRef);
        this.inputModel = new FormInputModel(recordRef);

        this.container = new VerticalLayoutContainer();
        this.container.mask();
        this.container.setScrollMode(ScrollSupport.ScrollMode.AUTOY);
        this.container.addAttachHandler(event -> {
            if(event.isAttached()) {
                structureSubscription = this.formStructure.subscribe(this::onStructureChanged);
            } else {
                structureSubscription.unsubscribe();
                structureSubscription = null;
            }
        });
    }

    private void onStructureChanged(Observable<FormStructure> observable) {
        if(!initialLoad && observable.isLoaded()) {
            onInitialLoad(observable.get());
        } else {
            // TODO: alert the user and prompt to update the form layout
        }
    }

    private void onInitialLoad(FormStructure formStructure) {
        initialLoad = true;
        container.unmask();

        viewModelBuilder = new FormInputViewModelBuilder(formStore, formStructure.getDatabase(), formStructure.getFormTree(), new LiveActivePeriodMemory());
        existingRecord = formStructure.getExistingRecord();

        formPanel = new FormPanel(formStore, formStructure.getFormTree(), inputModel.getRecordRef(), this, existingRecord);
        container.add(formPanel, new VerticalLayoutContainer.VerticalLayoutData(1, -1, new Margins(15, 25, 10, 15)));
        container.forceLayout();

        viewModel = viewModelBuilder.build(inputModel, existingRecord);
        formPanel.init(viewModel);
        formPanel.updateView(viewModel);
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    @Override
    public void updateModel(RecordRef recordRef, ResourceId fieldId, FieldInput value) {
        update(inputModel.update(recordRef, fieldId, value));
    }

    @Override
    public void addSubRecord(RecordRef subRecordRef) {
        update(inputModel.addSubRecord(subRecordRef));
    }

    @Override
    public void deleteSubRecord(RecordRef recordRef) {
        update(inputModel.deleteSubRecord(recordRef));
    }

    @Override
    public void changeActiveSubRecord(ResourceId fieldId, RecordRef newActiveRef) {
        update(inputModel.updateActiveSubRecord(fieldId, newActiveRef));
    }

    @Override
    public void updateSubModel(FormInputModel update) {
        update(inputModel.updateSubForm(update));
    }

    @Override
    public void validationRequested(RecordRef recordRef) {
        update(inputModel.validationRequested(recordRef));

    }

    @Override
    public void touchField(RecordRef recordRef, ResourceId fieldId) {
        update(inputModel.touch(recordRef, fieldId));
    }

    private void update(FormInputModel updatedModel) {
        this.inputModel = updatedModel;
        this.viewModel = viewModelBuilder.build(inputModel, existingRecord);
        formPanel.updateView(viewModel);
    }

    public boolean isValid() {
        if(viewModel == null) {
            return false;
        }
        return false;
    }

    public void save(CloseEvent.CloseHandler closeHandler) {
        // If the view model is still loading, ignore this click
        if(viewModel == null) {
            return;
        }

        if(!viewModel.isValid()) {
            invalidFeedback();
            return;
        }

        // Good to go...
        RecordTransaction tx = viewModel.buildTransaction();

        LOGGER.info("Submitting transaction: " + Json.stringify(tx));

        formStore.updateRecords(tx).then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                MessageBox box = new MessageBox(I18N.CONSTANTS.serverError(), I18N.CONSTANTS.errorOnServer());
                box.setModal(true);
                box.show();
            }

            @Override
            public void onSuccess(Void result) {
                closeHandler.onClose(new CloseEvent(null));
            }
        });
    }

    private void invalidFeedback() {

        update(inputModel.validationRequested());

        formPanel.scrollToFirstError();

        MessageBox box = new MessageBox(I18N.CONSTANTS.error(), I18N.CONSTANTS.pleaseFillInAllRequiredFields());
        box.setModal(true);
        box.show();


    }
}
