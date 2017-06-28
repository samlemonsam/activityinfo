package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.CloseEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModelBuilder;
import org.activityinfo.ui.client.input.viewModel.FormStructure;
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

        viewModelBuilder = new FormInputViewModelBuilder(formStore, formStructure);
        formPanel = new FormPanel(formStructure.getFormTree(), inputModel.getRecordRef(), this);
        container.add(formPanel, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
        container.forceLayout();

        viewModel = viewModelBuilder.build(inputModel);
        formPanel.update(viewModel);
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

    private void update(FormInputModel updatedModel) {
        this.inputModel = updatedModel;
        this.viewModel = viewModelBuilder.build(inputModel);
        formPanel.update(viewModel);
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
            MessageBox box = new MessageBox(I18N.CONSTANTS.error(), I18N.CONSTANTS.pleaseFillInAllRequiredFields());
            box.setModal(true);
            box.show();
            return;
        }

        // Good to go...
        formStore.updateRecords(viewModel.buildTransaction()).then(new AsyncCallback<Void>() {
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
}
